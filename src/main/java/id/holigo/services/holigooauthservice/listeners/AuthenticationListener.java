package id.holigo.services.holigooauthservice.listeners;

import java.io.IOException;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.Message;

import id.holigo.services.holigooauthservice.web.controllers.AuthenticationController;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import id.holigo.services.common.model.OauthAccessTokenDto;
import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.config.JmsConfig;
import id.holigo.services.holigooauthservice.domain.Client;
import id.holigo.services.holigooauthservice.repositories.ClientRepository;
import id.holigo.services.holigooauthservice.services.OauthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationListener {

    private final OauthService oauthService;

    private final JmsTemplate jmsTemplate;

    private final ClientRepository clientRepository;

    @JmsListener(destination = JmsConfig.CREATE_ACCESS_TOKEN_QUEUE)
    public void listen(@Payload UserAuthenticationDto userAuthenticationDto, @Headers MessageHeaders headers,
                       Message message) throws IOException, JmsException, JMSException {
        OauthAccessTokenDto oauthAccessTokenDto = AuthenticationController.userAuthentication(userAuthenticationDto, clientRepository, oauthService);
        jmsTemplate.convertAndSend(message.getJMSReplyTo(), oauthAccessTokenDto);

    }
}
