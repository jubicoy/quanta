package fi.jubic.quanta.models.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.Parameter;
import fi.jubic.quanta.models.Task;
import java.util.List;

@EasyValue
@JsonDeserialize(builder = InvocationResponse.Builder.class)
public abstract class InvocationResponse {

    public abstract Long getInvocationId();

    public abstract List<Parameter> getParameters();

    public abstract Task getTask();

    public abstract List<ColumnSelector> getColumnSelectors();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_InvocationResponse.Builder {

    }

    public static InvocationResponse of(Invocation invocation) {
        return InvocationResponse.builder()
                .setInvocationId(invocation.getId())
                .setParameters(invocation.getParameters())
                .setTask(invocation.getTask())
                .setColumnSelectors(invocation.getColumnSelectors())
                .build();
    }
}
