package fi.jubic.quanta.models.typemetadata;

import fi.jubic.easyvalue.EasyValue;

@EasyValue
public abstract class CsvTypeMetadata {
    public abstract String getComment();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_CsvTypeMetadata.Builder {

    }
}
