package id.holigo.services.holigooauthservice.services;

import java.util.ArrayList;
import java.util.Collection;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoligoUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        UserAuthenticationDto userAuthenticationDto = new UserAuthenticationDto();
        userAuthenticationDto.setPhoneNumber(phoneNumber);
        UserAuthenticationDto user;
        try {
            user = userService.getUser(userAuthenticationDto);
        } catch (JsonProcessingException | JMSException e) {
            throw new UsernameNotFoundException("Failed! User not found!");
        }
        if (user == null) {
            throw new UsernameNotFoundException("User not found!");
        } else {
            log.info("User -> {}", user);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getAuthorities().forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority)));
        return new User(user.getPhoneNumber(), user.getOneTimePassword(), authorities);
    }

}
