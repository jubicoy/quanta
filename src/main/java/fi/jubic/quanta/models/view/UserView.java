package fi.jubic.quanta.models.view;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.User;

@EasyValue
@JsonDeserialize(builder = UserView.Builder.class)
public abstract class UserView {
    public abstract Long getId();

    public abstract String getRole();

    public abstract String getName();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_UserView.Builder {
    }

    public static UserView of(User user) {
        return UserView.builder()
                .setId(user.getId())
                .setName(user.getName())
                .setRole(user.getRole())
                .build();
    }
}
