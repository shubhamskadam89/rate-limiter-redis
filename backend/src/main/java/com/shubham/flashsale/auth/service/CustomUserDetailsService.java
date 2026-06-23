package com.shubham.flashsale.auth.service;




import com.shubham.flashsale.auth.dto.UserDetailsImpl;
import com.shubham.flashsale.user.repository.UserRepository;
import com.shubham.flashsale.user.entity.User;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService  implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(
                        ()-> new UsernameNotFoundException("No such user exist")
                );
        return new UserDetailsImpl(user);
    }
}