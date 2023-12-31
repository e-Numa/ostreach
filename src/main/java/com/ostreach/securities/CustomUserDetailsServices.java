package com.ostreach.securities;

import com.ostreach.entities.model.UserEntity;
import com.ostreach.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServices implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity users = userRepository.findUserEntityByEmail(email)
                                        .orElseThrow(() -> new UsernameNotFoundException("User not Found!"));

        return new User(users.getEmail(), users.getPassword(), mapRolesToAuthorities(users));
    }

    public Collection<GrantedAuthority> mapRolesToAuthorities(UserEntity user){
        return Collections.singleton(new SimpleGrantedAuthority(String.valueOf(user.getRoles())));
    }
}
