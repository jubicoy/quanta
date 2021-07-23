package fi.jubic.quanta.models;

import fi.jubic.easyvalue.EasyValue;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EasyValue
public abstract class AnomalyFilter {
    public abstract String getColumn();

    public abstract String getFilterCondition();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_AnomalyFilter.Builder {

    }

    public static List<AnomalyFilter> parse(
            List<AnomalyQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(AnomalyQuerySelector::getIsFilter)
                .map(selector -> AnomalyFilter.builder()
                        .setColumn(
                                transformFilterToJsonbFormat(
                                        Objects.requireNonNull(selector
                                                .getAnomalyResultSelector())
                                                .getColumnName()
                                )
                        )
                        .setFilterCondition(selector.getFilterCondition())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static String transformFilterToJsonbFormat(String column) {
        Pattern pattern = Pattern.compile(
                "(sample)\\s?(->)\\s?'([a-zA-Z0-9-_]+)'\\s?"
        );

        Matcher matcher = pattern.matcher(column);
        if (!matcher.matches()) {
            return column;
        }

        return String.format(
                "%s ->> '%s' ",
                matcher.group(1),
                matcher.group(3)
        );
    }
}
