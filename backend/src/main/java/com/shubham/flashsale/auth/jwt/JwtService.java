package com.shubham.flashsale.auth.jwt;

import com.shubham.flashsale.auth.dto.UserDetailsImpl;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final JwtProperties jwtProperties;

  public String generateToken(UserDetails principal) {
    Instant now = Instant.now();
    UserDetailsImpl userDetails = (UserDetailsImpl) principal;
    log.debug("Generating JWT token for user uuid={}", userDetails.user().getUuid());
    JwtClaimsSet claimsSet =
        JwtClaimsSet.builder()
            .subject(userDetails.user().getUuid())
            .claim("role", userDetails.user().getRole().name())
            .issuedAt(now)
            .expiresAt(now.plusMillis(jwtProperties.getExpiration()))
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

    return jwtEncoder.encode(JwtEncoderParameters.from(header, claimsSet)).getTokenValue();
  }

  public String extractUserUuid(String token) {
    log.debug("Extracting user UUID from JWT token");
    return jwtDecoder.decode(token).getSubject();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    Jwt jwt = jwtDecoder.decode(token);

    UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;

    boolean valid = jwt.getSubject().equals(userDetailsImpl.user().getUuid());
    log.debug(
        "Validated JWT token for user uuid={}, valid={}", userDetailsImpl.user().getUuid(), valid);
    return valid;
  }
}
