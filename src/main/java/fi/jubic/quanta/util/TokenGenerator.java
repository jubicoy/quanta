package fi.jubic.quanta.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TokenGenerator {

    @SuppressFBWarnings(
            value = "WEAK_MESSAGE_DIGEST_SHA1",
            justification = "SHA-1 is used here for generating a digital signature, "
                    + "not hashing password"
    )
    public static String generate() {
        SecureRandom secureRandom;
        String token = null;
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        String random = Integer.toString(secureRandom.nextInt());

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] encodedHash = sha.digest(
                    random.getBytes(StandardCharsets.UTF_8)
            );
            token = bytesToHex(encodedHash);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return token;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
