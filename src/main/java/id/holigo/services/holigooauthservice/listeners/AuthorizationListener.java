package id.holigo.services.holigooauthservice.listeners;

import java.util.Optional;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthorizationListener {

    private final JmsTemplate jmsTemplate;

    private final AccessTokenRepository accessTokenRepository;

    @JmsListener(destination = JmsConfig.OAUTH_QUEUE)
    public void listen(@Payload OauthDto oauthDto, @Headers MessageHeaders headers, Message message)
            throws JmsException, JMSException {
        log.info("listen authorization is running.....");
        try {
            String token = oauthDto.getToken();
            log.info("token -> {}", token);
            Algorithm algorithm = OauthServiceImpl.ALGORITHM;
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            String subject = decodedJWT.getSubject();
            String[] authorities = decodedJWT.getClaim("authorities").asArray(String.class);
            Optional<AccessToken> fetchAccessToken = accessTokenRepository
                    .findById(UUID.fromString(decodedJWT.getId()));
            if (fetchAccessToken.isPresent()) {
                log.info("accessToken isPresent");
                AccessToken accessToken = fetchAccessToken.get();
                if (accessToken.getRevoked()) {
                    log.info("revoked detected...");
                    throw new Exception("Please login...");
                } else {
                    log.info("Valid");
                    oauthDto.setValid(true);
                    oauthDto.setId(decodedJWT.getId());
                    oauthDto.setSubject(subject);
                    oauthDto.setAuthorities(authorities);
                    oauthDto.setExpiredAt(decodedJWT.getExpiresAt());
                    oauthDto.setPayload(decodedJWT.getPayload());
                }

            } else {
                log.info("accessToken not present...");
                throw new Exception("Please login...");
            }

        } catch (Exception e) {
            log.info("Exception in listen : " + e.getMessage());
            oauthDto.setValid(false);
        }
        jmsTemplate.convertAndSend(message.getJMSReplyTo(), oauthDto);
        log.info("Reply sent, oauthDto -> {}", oauthDto);

        jmsTemplate.convertAndSend(message.getJMSReplyTo(), oauthDto);
    }

}
