package org.adyl.security.services;

import org.adyl.security.details.StoreUserDetails;
import org.adyl.security.repositories.StoreUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StoreUserDetailsService implements UserDetailsService {
    @Autowired
    private StoreUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new StoreUserDetails(repository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("Such user was not found!")));
    }
}
