package id.holigo.services.holigooauthservice.listeners;

import java.util.Optional;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import id.holigo.services.holigooauthservice.web.exceptions.AuthenticationException;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import id.holigo.services.common.model.OauthDto;
import id.holigo.services.holigooauthservice.config.JmsConfig;
import id.holigo.services.holigooauthservice.domain.AccessToken;
import id.holigo.services.holigooauthservice.repositories.AccessTokenRepository;
import id.holigo.services.holigooauthservice.services.OauthServiceImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AuthorizationListener {

    private final JmsTemplate jmsTemplate;

    private final AccessTokenRepository accessTokenRepository;

    @JmsListener(destination = JmsConfig.OAUTH_QUEUE)
    public void listen(@Payload OauthDto oauthDto, @Headers MessageHeaders headers, Message message)
            throws JmsException, JMSException {
        try {
            String token = oauthDto.getToken();
            Algorithm algorithm = OauthServiceImpl.ALGORITHM;
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            String subject = decodedJWT.getSubject();
            String[] authorities = decodedJWT.getClaim("authorities").asArray(String.class);
            Optional<AccessToken> fetchAccessToken = accessTokenRepository
                    .findById(UUID.fromString(decodedJWT.getId()));
            if (fetchAccessToken.isPresent()) {
                AccessToken accessToken = fetchAccessToken.get();
                if (accessToken.getRevoked()) {
                    throw new AuthenticationException();
                } else {
                    oauthDto.setValid(true);
                    oauthDto.setId(decodedJWT.getId());
                    oauthDto.setSubject(subject);
                    oauthDto.setAuthorities(authorities);
                    oauthDto.setExpiredAt(decodedJWT.getExpiresAt());
                    oauthDto.setPayload(decodedJWT.getPayload());
                }

            } else {
                throw new AuthenticationException();
            }

        } catch (Exception e) {
            oauthDto.setValid(false);
        }
        jmsTemplate.convertAndSend(message.getJMSReplyTo(), oauthDto);
        jmsTemplate.convertAndSend(message.getJMSReplyTo(), oauthDto);
    }

}
