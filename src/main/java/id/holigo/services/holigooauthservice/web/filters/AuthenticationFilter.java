package id.holigo.services.holigooauthservice.web.filters;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.Transactional;

import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.domain.AccessToken;
import id.holigo.services.holigooauthservice.domain.Client;
import id.holigo.services.holigooauthservice.domain.RefreshToken;
import id.holigo.services.holigooauthservice.repositories.ClientRepository;
import id.holigo.services.holigooauthservice.services.AccessTokenService;
import id.holigo.services.holigooauthservice.services.AccessTokenServiceImpl;
import id.holigo.services.holigooauthservice.services.OauthServiceImpl;
import id.holigo.services.holigooauthservice.services.RefreshTokenService;
import id.holigo.services.holigooauthservice.services.RefreshTokenServiceImpl;
import id.holigo.services.holigooauthservice.services.user.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final static String HEADER_USER_LOCATION = "/api/v1/users/";

    private final AuthenticationManager authenticationManager;

    private final ClientRepository clientRepository;

    private final UserService userService;

    private final AccessTokenService accessTokenService;

    private final RefreshTokenService refreshTokenService;

    public AuthenticationFilter(AuthenticationManager authenticationManager, ClientRepository clientRepository,
                                UserService userService, AccessTokenService accessTokenService,
                                RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.clientRepository = clientRepository;
        this.userService = userService;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String phoneNumber = request.getParameter("phoneNumber");
        String oneTimePassword = request.getParameter("oneTimePassword");
        UUID clientId = UUID.fromString(request.getParameter("clientId"));
        String clientSecret = request.getParameter("clientSecret");
        Optional<Client> fetchClient = clientRepository.findByIdAndSecret(clientId, clientSecret);
        if (fetchClient.isEmpty()) {
            response.setStatus(401);
            return null;
        }
        request.setAttribute("client", fetchClient.get());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, oneTimePassword);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Transactional
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) throws IOException {

        Client client = (Client) request.getAttribute("client");
        UserAuthenticationDto userAuthenticationDto = new UserAuthenticationDto();
        User user = (User) authentication.getPrincipal();
        userAuthenticationDto.setPhoneNumber(request.getParameter("phoneNumber"));
        try {
            userAuthenticationDto = userService.getUser(userAuthenticationDto);
        } catch (JMSException e) {
            throw new IOException("User not found!");
        }

        AccessToken accessTokenObj = accessTokenService.createAccessToken(userAuthenticationDto, client);

        RefreshToken refreshTokenObj = refreshTokenService.createRefreshToken(accessTokenObj);

        String type = "Bearer";
        String accessToken = this.accessToken(user, userAuthenticationDto, accessTokenObj, "login");
        String refreshToken = this.refreshToken(userAuthenticationDto, refreshTokenObj, "login");
        Long expiresIn = AccessTokenServiceImpl.ACCESS_TOKEN_EXPIRES;
        response.setStatus(201);
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("type", type);
        tokens.put("accessToken", accessToken);
        tokens.put("expiresIn", expiresIn);
        tokens.put("refreshToken", refreshToken);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setHeader("Location", HEADER_USER_LOCATION + userAuthenticationDto.getId());
        userService.resetOtp(userAuthenticationDto);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    private String accessToken(User user, UserAuthenticationDto userAuthenticationDto, AccessToken accessTokenObj,
                               String issuer) {
        return JWT.create().withSubject(userAuthenticationDto.getId().toString())
                .withJWTId(accessTokenObj.getId().toString())
                .withExpiresAt(new Date(AccessTokenServiceImpl.ACCESS_TOKEN_EXPIRES)).withIssuer(issuer)
                .withClaim("authorities",
                        user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .withClaim("type", userAuthenticationDto.getType())
                .withClaim("group", userAuthenticationDto.getUserGroup().toString())
                .sign(OauthServiceImpl.ALGORITHM);
    }

    private String refreshToken(UserAuthenticationDto userAuthenticationDto, RefreshToken refreshTokenObj,
                                String issuer) {
        return JWT.create().withSubject(userAuthenticationDto.getId().toString())
                .withExpiresAt(new Date(RefreshTokenServiceImpl.REFRESH_TOKEN_EXPIRES))
                .withJWTId(refreshTokenObj.getId().toString()).withIssuer(issuer)
                .sign(OauthServiceImpl.ALGORITHM);
    }

}
