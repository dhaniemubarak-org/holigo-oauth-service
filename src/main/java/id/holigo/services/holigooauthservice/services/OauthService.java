package id.holigo.services.holigooauthservice.services;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import id.holigo.services.common.model.OauthAccessTokenDto;
import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.domain.Client;
import org.springframework.security.core.userdetails.User;

public interface OauthService {
    OauthAccessTokenDto create(UserAuthenticationDto userAuthenticationDto, User user, Client client,
            String issuer) throws IOException;

    OauthAccessTokenDto createAcccessTokenWithAuthentication(UserAuthenticationDto userAuthenticationDto, Client client,
            String issuer) throws IOException;

    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken(String phoneNumber, String oneTimePassword);

    Authentication authentication(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken);
}
