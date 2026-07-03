package com.shubham.flashsale.auth.service;




import com.shubham.flashsale.auth.dto.UserDetailsImpl;
import com.shubham.flashsale.user.repository.UserRepository;
import com.shubham.flashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService  implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username (email): {}", username);
        User user = userRepository.findByEmail(username)
                .orElseThrow(
                        ()-> {
                            log.warn("User not found by email: {}", username);
                            return new UsernameNotFoundException("No such user exist");
                        }
                );
        log.debug("User loaded successfully: {}", user.getEmail());
        return new UserDetailsImpl(user);
    }
}