package fi.jubic.quanta.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Random;

@SuppressFBWarnings(
        value = "PREDICTABLE_RANDOM",
        justification = "Not used for security"
)
public class StringUtil {
    private static final Random RANDOM = new Random();
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
