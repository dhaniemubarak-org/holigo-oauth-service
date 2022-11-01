package id.holigo.services.holigooauthservice.services;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.domain.AccessToken;
import id.holigo.services.holigooauthservice.domain.Client;
import id.holigo.services.holigooauthservice.repositories.AccessTokenRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    public static final Long ACCESS_TOKEN_EXPIRES = System.currentTimeMillis() + 31557600000L;

    private final AccessTokenRepository accessTokenRepository;

    @Override
    public AccessToken createAccessToken(UserAuthenticationDto userAuthenticationDto, Client client)
            throws IOException {
        AccessToken accessTokenObj = new AccessToken();
        accessTokenObj.setClient(client);
        accessTokenObj.setName(userAuthenticationDto.getPhoneNumber());
        accessTokenObj.setRevoked(false);
        accessTokenObj.setExpiredAt(new Timestamp(ACCESS_TOKEN_EXPIRES));
        accessTokenObj.setUserId(userAuthenticationDto.getId());
        accessTokenObj.setScopes(userAuthenticationDto.getAuthorities().toString());

        AccessToken savedAccessToken = accessTokenRepository.save(accessTokenObj);
        if (savedAccessToken.getId() == null) {
            throw new IOException("Opps, Internal server error");
        }
        accessTokenRepository.revokeAccessToken(1, Timestamp.valueOf(LocalDateTime.now()), userAuthenticationDto.getId(), savedAccessToken.getId());
        return savedAccessToken;
    }

}
