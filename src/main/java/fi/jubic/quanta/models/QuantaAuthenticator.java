package fi.jubic.quanta.models;

import fi.jubic.quanta.controller.ExternalClientController;
import fi.jubic.snoozy.auth.implementation.StatefulAuthenticator;
import fi.jubic.snoozy.auth.implementation.Token;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class QuantaAuthenticator extends StatefulAuthenticator<User> {
    private final List<Token<User>> tokens;
    private final ExternalClientController externalClientController;

    @Inject
    public QuantaAuthenticator(
            ExternalClientController externalClientController
    ) {
        this.tokens = new CopyOnWriteArrayList<>();
        this.externalClientController = externalClientController;
    }

    @Override
    public Optional<User> authenticate(String token) {
        return super.authenticate(token)
                .or(
                        () -> tokens.stream()
                                .filter(t -> t.getToken().equals(token))
                                .findFirst()
                                .map(Token::getUser)
                );
    }

    public void reloadExternalClients() {
        externalClientController
                .getExternalClients()
                .stream()
                .map(
                        externalClient -> new Token<>(
                        externalClient.getUser(),
                        externalClient.getToken(),
                        LocalDateTime.now().plusYears(100))
                )
                .filter(token -> !tokens.contains(token))
                .forEach(
                        token -> {
                            tokens.add(token);
                            authenticate(token.getToken());
                        }
                );
    }

}
