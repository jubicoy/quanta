package fi.jubic.quanta.models.view;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

@EasyValue
@JsonDeserialize(builder = FileUploadResponse.Builder.class)
public abstract class FileUploadResponse {
    public abstract String getFileName();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_FileUploadResponse.Builder {

    }
}
