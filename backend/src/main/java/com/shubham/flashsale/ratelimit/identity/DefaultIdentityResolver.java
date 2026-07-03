package com.shubham.flashsale.ratelimit.identity;

import com.shubham.flashsale.auth.dto.UserDetailsImpl;
import com.shubham.flashsale.common.entity.BaseEntity;
import com.shubham.flashsale.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultIdentityResolver implements IdentityResolver{


    @Override
    public RateLimitIdentity resolve(HttpServletRequest request) {

        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();


        if(auth!=null && auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken)){

            if (auth.getPrincipal() instanceof Jwt jwt) {
                RateLimitIdentity identity = new RateLimitIdentity(
                        IdentityType.USER,
                        jwt.getSubject()
                );
                log.debug("Resolved rate limit identity: {}", identity);
                return identity;
            }
        }
        RateLimitIdentity identity = new RateLimitIdentity(
                IdentityType.IP,
                request.getRemoteAddr()
        );
        log.debug("Resolved rate limit identity: {}", identity);
        return identity;
    }
}