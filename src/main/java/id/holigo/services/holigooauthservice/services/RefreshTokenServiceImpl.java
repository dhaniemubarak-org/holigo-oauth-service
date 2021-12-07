package id.holigo.services.holigooauthservice.services;

import java.io.IOException;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.holigo.services.holigooauthservice.domain.AccessToken;
import id.holigo.services.holigooauthservice.domain.RefreshToken;
import id.holigo.services.holigooauthservice.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    public static final Long REFRESH_TOKEN_EXPIRES = System.currentTimeMillis() + 31557600000L * 2;
    @Autowired
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken createRefreshToken(AccessToken accessToken) throws IOException {
        RefreshToken refreshTokenObj = new RefreshToken();
        refreshTokenObj.setRefoked(false);
        refreshTokenObj.setAccessToken(accessToken);
        refreshTokenObj.setExpiredAt(new Timestamp(REFRESH_TOKEN_EXPIRES));

        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshTokenObj);
        if (savedRefreshToken.getId() == null) {
            throw new IOException("Opps, Internal server error");
        }
        return savedRefreshToken;
    }

}
