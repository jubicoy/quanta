package fi.jubic.quanta.models.view;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.models.Task;

import javax.annotation.Nullable;

@EasyValue
@JsonDeserialize(builder = ExternalClientView.Builder.class)
public abstract class ExternalClientView {

    public abstract Long getId();

    public abstract String getName();

    public abstract String getToken();

    public abstract String getDescription();

    @Nullable
    public abstract Task getTask();

    public abstract UserView getCreatedBy();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_ExternalClientView.Builder {

    }

    public static ExternalClientView of(ExternalClient externalClient) {
        return ExternalClientView.builder()
                .setId(externalClient.getId())
                .setName(externalClient.getName())
                .setToken(externalClient.getToken())
                .setDescription(externalClient.getDescription())
                .setTask(externalClient.getTask())
                .setCreatedBy(
                        UserView.of(externalClient.getUser())
                )
                .build();
    }
}
