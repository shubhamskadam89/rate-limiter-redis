package com.shubham.flashsale.auth.service;

import com.shubham.flashsale.auth.dto.AuthResponse;
import com.shubham.flashsale.auth.dto.RefreshRequest;
import com.shubham.flashsale.auth.dto.UserDetailsImpl;
import com.shubham.flashsale.auth.entity.RefreshToken;
import com.shubham.flashsale.auth.jwt.JwtProperties;
import com.shubham.flashsale.auth.jwt.JwtService;
import com.shubham.flashsale.auth.repository.RefreshTokenRepository;
import com.shubham.flashsale.common.service.CommonAuthService;
import com.shubham.flashsale.exception.user.UserAlreadyExistsException;
import com.shubham.flashsale.user.dto.LoginDto;
import com.shubham.flashsale.user.dto.RegistrartionDto;
import com.shubham.flashsale.user.dto.UserResponseDto;
import com.shubham.flashsale.user.entity.User;
import com.shubham.flashsale.user.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final RefreshTokenRepository refreshTokenRepository;
  private final RefreshTokenService refreshTokenService;
  private final CommonAuthService commonAuthService;

  public UserResponseDto registerUser(RegistrartionDto registrartionDto) {
    log.info("USER REGISTRATION REQUEST FETCHED");
    if (userRepository.existsByEmail(registrartionDto.email())) {
      log.debug("USER WITH {} EMAIL EXIST", registrartionDto.email());
      throw new UserAlreadyExistsException(registrartionDto.email());
    }
    log.info("ALL CHECKS PASSED FOR REGISTRATION");
    User user =
        User.builder()
            .email(registrartionDto.email())
            .fullName(registrartionDto.fullName())
            .passwordHash(passwordEncoder.encode(registrartionDto.password()))
            .role(registrartionDto.role())
            .isActive(true) // later verify using otp on mail
            .build();

    userRepository.save(user);
    log.warn("{} SAVED TO DATABASE", user.getFullName());

    return new UserResponseDto(
        UUID.fromString(user.getUuid()),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        user.getIsActive());
  }

  public AuthResponse loginUser(LoginDto loginDto) {
    log.info("USER LOGIN REQUEST FETCHED");
    if (!userRepository.existsByEmail(loginDto.email())) {
      log.debug("USER WITH {} EMAIL DOES NOT EXIST", loginDto.email());
      throw new NoSuchElementException(loginDto.email());
    }
    log.info("ALL CHECKS PASSED FOR LOGIN");

    // issue jwt token and all
    var authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));

    String accessToken = jwtService.generateToken((UserDetails) authentication.getPrincipal());

    User user = userRepository.findByEmail(loginDto.email()).orElseThrow();

    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

    return new AuthResponse(
        accessToken,
        refreshToken.getToken(),
        "Bearer",
        jwtProperties.getExpiration() / 1000,
        user.getRole().name());
  }

  private String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }

  public AuthResponse refresh(RefreshRequest request) {
    log.info("Processing token refresh request");
    RefreshToken oldToken = refreshTokenService.validateRefreshToken(request.refreshToken());

    RefreshToken newToken = refreshTokenService.rotateRefreshToken(oldToken);

    User user = oldToken.getUser();

    String accessToken = jwtService.generateToken(new UserDetailsImpl(user));

    log.info("Token refresh successful for user uuid={}", user.getUuid());
    return new AuthResponse(
        accessToken,
        newToken.getToken(),
        "Bearer",
        jwtProperties.getExpiration() / 1000,
        user.getRole().name());
  }

  public void logout(RefreshRequest request) {
    log.info("Processing user logout request");
    RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.refreshToken());

    refreshTokenService.revokeRefreshToken(refreshToken);
    log.info("User logout completed successfully");
  }

  public UserResponseDto getMe() {
    log.debug("Fetching current user profile (getMe)");
    User user = commonAuthService.getCurrentUser();
    UserResponseDto dto =
        new UserResponseDto(
            UUID.fromString(user.getUuid()),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getIsActive());
    return dto;
  }
}
