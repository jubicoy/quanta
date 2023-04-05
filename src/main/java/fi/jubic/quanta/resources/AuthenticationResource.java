package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.UserController;
import fi.jubic.quanta.models.LoginRequest;
import fi.jubic.quanta.models.QuantaAuthenticator;
import fi.jubic.quanta.models.User;
import fi.jubic.quanta.util.HashUtil;
import fi.jubic.snoozy.auth.implementation.Token;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource {
    private final QuantaAuthenticator authenticator;
    private final UserController userController;

    @Inject
    AuthenticationResource(
            QuantaAuthenticator authenticator,
            UserController userController
    ) {
        this.authenticator = authenticator;
        this.userController = userController;
    }

    @Path("/login")
    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Token<User> login(LoginRequest loginRequest) {
        User user = userController
                .getUserByName(loginRequest.getUsername())
                .orElseThrow(BadRequestException::new);

        if (!HashUtil.test(
                loginRequest.getPassword(),
                user.getPasswordHash(),
                user.getSalt()
        )) {
            throw new BadRequestException();
        }

        Token<User> token = new Token<>(user);
        authenticator.addToken(token);

        return token;
    }

    @Path("/me")
    @GET
    public Token<User> getMe(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        User user = authenticator.authenticate(authorization)
                .orElseThrow(NotFoundException::new);

        return new Token<>(user, authorization);
    }

    @Path("/logout")
    @POST
    public Response logout(
            @Context User user,
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        authenticator.revokeToken(
                new Token<>(user, authorization)
        );

        return Response.ok()
                .build();
    }
}
