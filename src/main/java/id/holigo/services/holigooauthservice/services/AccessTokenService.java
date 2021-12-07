package id.holigo.services.holigooauthservice.services;

import java.io.IOException;

import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.domain.AccessToken;
import id.holigo.services.holigooauthservice.domain.Client;

public interface AccessTokenService {
    AccessToken createAccessToken(UserAuthenticationDto userAuthenticationDto, Client client) throws IOException;
}
