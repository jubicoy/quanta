package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

@EasyValue
@JsonDeserialize(builder = LoginRequest.Builder.class)
public abstract class LoginRequest {
    public abstract String getUsername();

    public abstract String getPassword();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_LoginRequest.Builder {
    }

}
