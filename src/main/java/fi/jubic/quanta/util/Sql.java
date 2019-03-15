package fi.jubic.quanta.util;

public class Sql {
    public static String sanitize(String parameter) {
        String sanitized = parameter.replace("`", "")
                .replace(" ", "_")
                .replace("--", "")
                .replace("'", "")
                .replace("\"", "");

        return String.format("%s", sanitized);
    }
}
