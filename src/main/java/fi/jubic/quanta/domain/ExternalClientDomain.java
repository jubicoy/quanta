package fi.jubic.quanta.domain;

import fi.jubic.quanta.exception.AuthorizationException;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.util.TokenGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

@Singleton
public class ExternalClientDomain {
    private static final String NAMING_PATTERN_REGEX = "^[a-zA-Z0-9-_]+$";

    @Inject
    ExternalClientDomain() {
    }

    public ExternalClient validate(ExternalClient externalClient) {
        if (Objects.nonNull(externalClient.getDeletedAt())) {
            throw new AuthorizationException("Invalid client");
        }
        return externalClient;
    }

    public ExternalClient create(ExternalClient externalClient) {
        // Validate name
        if (!Pattern.matches(NAMING_PATTERN_REGEX, externalClient.getName())) {
            throw new InputException("Name is invalid");
        }

        return externalClient
                .toBuilder()
                .setId(0L)
                .setDeletedAt(null)
                .setToken(TokenGenerator.generate())
                .build();
    }

    public ExternalClient softDelete(ExternalClient externalClient) {
        ExternalClient validatedClient = validate(externalClient);
        return validatedClient
                .toBuilder()
                .setDeletedAt(Instant.now())
                .build();
    }
}
