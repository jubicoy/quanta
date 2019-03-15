package fi.jubic.quanta.models;

public enum ColumnSelectorType {
    input,
    output;

    public static ColumnSelectorType parse(String columnSelectorType) {
        switch (columnSelectorType.toLowerCase()) {
            case "input": return input;
            case "output": return output;

            default: return null;
        }
    }
}
