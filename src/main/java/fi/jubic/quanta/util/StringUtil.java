package fi.jubic.quanta.util;

import java.security.SecureRandom;

public class StringUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMS = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static String alphaNumericIdentifier(int length) {
        return RANDOM.ints(length, 0, ALPHANUMS.length())
                .boxed()
                .reduce(
                        new StringBuilder(),
                        (sb, index) -> sb.append(ALPHANUMS.charAt(index)),
                        (a, b) -> a.append(b.toString())
                )
                .toString();
    }
}
