/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : CustomUserDetailsService
 * DESCRIPTION:
 *   Spring Security UserDetailsService implementation.
 *   Loads user details from the database by email address.
 *   Used by the authentication provider during login validation.
 * ================================================================ */
package com.capg.RechargeHub.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.capg.RechargeHub.entity.User;
import com.capg.RechargeHub.repository.UserRepository;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        return user.map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("user not found with email :" + username));
    }
}
