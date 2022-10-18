package id.holigo.services.holigooauthservice.services;

import org.springframework.security.authentication.AuthenticationManager;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.User;
import id.holigo.services.common.model.OauthAccessTokenDto;
import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.domain.AccessToken;
import id.holigo.services.holigooauthservice.domain.Client;
import id.holigo.services.holigooauthservice.domain.RefreshToken;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OauthServiceImpl implements OauthService {

    public static final Algorithm ALGORITHM = Algorithm.HMAC256("secret".getBytes());

    private final AuthenticationManager authenticationManager;

    private final AccessTokenService accessTokenService;

    private final RefreshTokenService refreshTokenService;

    @Override
    public OauthAccessTokenDto create(UserAuthenticationDto userAuthenticationDto, User user, Client client,
                                      String issuer) throws IOException {
        AccessToken accessTokenObj = accessTokenService.createAccessToken(userAuthenticationDto, client);
        RefreshToken refreshTokenObj = refreshTokenService.createRefreshToken(accessTokenObj);
        String accessToken = this.accessToken(user, userAuthenticationDto, accessTokenObj, issuer);
        String refreshToken = this.refreshToken(userAuthenticationDto, refreshTokenObj, issuer);
        return OauthAccessTokenDto.builder().type("Bearer").accessToken(accessToken)
                .expiresIn(AccessTokenServiceImpl.ACCESS_TOKEN_EXPIRES).refreshToken(refreshToken)
                .build();
    }

    @Override
    public UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken(String phoneNumber,
                                                                                   String oneTimePassword) {
        return new UsernamePasswordAuthenticationToken(phoneNumber, oneTimePassword);
    }

    @Override
    public Authentication authentication(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    public OauthAccessTokenDto createAccessTokenWithAuthentication(UserAuthenticationDto userAuthenticationDto,
                                                                   Client client, String issuer) throws IOException {
        UsernamePasswordAuthenticationToken authenticationToken = this.usernamePasswordAuthenticationToken(
                userAuthenticationDto.getPhoneNumber(), userAuthenticationDto.getOneTimePassword());
        Authentication authentication = this.authentication(authenticationToken);
        authenticationManager.authenticate(authenticationToken);
        User user = (User) authentication.getPrincipal();
        return this.create(userAuthenticationDto, user, client, issuer);
    }

    private String accessToken(User user, UserAuthenticationDto userAuthenticationDto, AccessToken accessTokenObj,
                               String issuer) {
        return JWT.create().withSubject(userAuthenticationDto.getId().toString())
                .withJWTId(accessTokenObj.getId().toString())
                .withExpiresAt(new Date(AccessTokenServiceImpl.ACCESS_TOKEN_EXPIRES)).withIssuer(issuer)
//                                .withClaim("authorities", user.getAuthorities().stream()
//                                                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .withClaim("authorities", userAuthenticationDto.getAuthorities().stream().toList())
                .withClaim("type", userAuthenticationDto.getType())
                .withClaim("group", userAuthenticationDto.getUserGroup().toString())
                .sign(ALGORITHM);
    }

    private String refreshToken(UserAuthenticationDto userAuthenticationDto, RefreshToken refreshTokenObj,
                                String issuer) {
        return JWT.create().withSubject(userAuthenticationDto.getId().toString())
                .withExpiresAt(new Date(RefreshTokenServiceImpl.REFRESH_TOKEN_EXPIRES))
                .withJWTId(refreshTokenObj.getId().toString()).withIssuer(issuer).sign(ALGORITHM);
    }

}
