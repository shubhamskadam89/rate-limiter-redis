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
import org.springframework.stereotype.Component;

@Component
public class DefaultIdentityResolver implements IdentityResolver{


    @Override
    public RateLimitIdentity resolve(HttpServletRequest request) {

        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();


        if(auth!=null && auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken)){

            Jwt jwt = (Jwt) auth.getPrincipal();
            Long userId = jwt.getClaim("userId");

            return new RateLimitIdentity(
                    IdentityType.USER,
                    String.valueOf(userId)
            );
        }
        return new RateLimitIdentity(
                IdentityType.IP,
                request.getRemoteAddr()
        );
    }
}