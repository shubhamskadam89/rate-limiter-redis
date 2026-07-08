package com.shubham.flashsale.common.service;

import com.shubham.flashsale.user.entity.User;
import com.shubham.flashsale.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonAuthService {

  private final UserRepository userRepository;

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    // Spring's oauth2ResourceServer sets the principal as a Jwt token, NOT UserDetailsImpl.
    // We extract the subject claim (which is the user's email) and load the User from the DB.
    if (!(principal instanceof Jwt jwt)) {
      log.error("Principal is not a Jwt token. type={}", principal.getClass().getName());
      throw new AccessDeniedException("Authenticated user not found");
    }

    String uuid = jwt.getSubject();
    log.debug("Resolved current user id={} from JWT subject", uuid);

    return userRepository
        .findByUuid(uuid)
        .orElseThrow(
            () -> {
              log.error("No user found in DB for JWT subject uuid={}", uuid);
              return new AccessDeniedException("User not found: " + uuid);
            });
  }
}
