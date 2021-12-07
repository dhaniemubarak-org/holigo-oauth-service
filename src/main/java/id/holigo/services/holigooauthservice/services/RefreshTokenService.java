package id.holigo.services.holigooauthservice.services;

import java.io.IOException;

import id.holigo.services.holigooauthservice.domain.AccessToken;
import id.holigo.services.holigooauthservice.domain.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(AccessToken accessToken) throws IOException;
}
