package id.holigo.services.holigooauthservice.web.controllers;

import id.holigo.services.common.model.OauthAccessTokenDto;
import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.domain.Client;
import id.holigo.services.holigooauthservice.repositories.ClientRepository;
import id.holigo.services.holigooauthservice.services.OauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
public class AuthenticationController {

    private ClientRepository clientRepository;

    private OauthService oauthService;

    @Autowired
    public void setOauthService(OauthService oauthService) {
        this.oauthService = oauthService;
    }

    @Autowired
    public void setClientRepository(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public ResponseEntity<OauthAccessTokenDto> createAccessToken(@RequestBody UserAuthenticationDto userAuthenticationDto) throws IOException {
        return new ResponseEntity<>(userAuthentication(userAuthenticationDto, clientRepository, oauthService), HttpStatus.CREATED);
    }

    public static OauthAccessTokenDto userAuthentication(@RequestBody UserAuthenticationDto userAuthenticationDto, ClientRepository clientRepository, OauthService oauthService) throws IOException {
        OauthAccessTokenDto oauthAccessTokenDto = new OauthAccessTokenDto();
        Optional<Client> fetchClient = clientRepository.findByName("Holigo Mobile Apps");

        if (fetchClient.isPresent()) {
            Client client = fetchClient.get();
            try {
                oauthAccessTokenDto = oauthService.createAccessTokenWithAuthentication(userAuthenticationDto, client, "register");
            } catch (IOException e) {
                throw new IOException(e.getMessage());
            }
        }
        return oauthAccessTokenDto;
    }
}
