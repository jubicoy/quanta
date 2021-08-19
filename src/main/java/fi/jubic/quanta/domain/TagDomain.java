package fi.jubic.quanta.domain;

import fi.jubic.quanta.exception.InputException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.regex.Pattern;

@Singleton
public class TagDomain {
    private static final String NAMING_PATTERN_REGEX = "^[a-zA-Z0-9-_]+$";

    @Inject
    TagDomain() {

    }

    public Set<String> validate(Set<String> tagNames) {
        // Validate Tag name
        tagNames.forEach((name) -> {
            if (!Pattern.matches(NAMING_PATTERN_REGEX, name)) {
                throw new InputException("Tag's name is invalid");
            }
        });
        return tagNames;
    }

}
