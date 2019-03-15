package fi.jubic.quanta.models;

public enum TimeSeriesModifier {
    avg,
    min,
    max,
    sum,
    group_by,
    where;

    public static TimeSeriesModifier parse(String timeSeriesModifier) {
        switch (timeSeriesModifier.toLowerCase()) {
            case "avg": return avg;
            case "min": return min;
            case "max": return max;
            case "sum": return sum;
            case "group_by": return group_by;
            case "where": return where;

            default: return null;
        }
    }
}
