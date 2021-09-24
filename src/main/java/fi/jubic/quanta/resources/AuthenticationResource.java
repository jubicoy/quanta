package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.UserController;
import fi.jubic.quanta.models.LoginRequest;
import fi.jubic.quanta.models.QuantaAuthenticator;
import fi.jubic.quanta.models.User;
import fi.jubic.quanta.util.HashUtil;
import fi.jubic.snoozy.auth.implementation.Token;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
