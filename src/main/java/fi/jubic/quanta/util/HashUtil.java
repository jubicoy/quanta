package fi.jubic.quanta.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;

public class HashUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                saltBytes,
                65536,
                128
        );
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            return Base64.getEncoder().encodeToString(
                    factory.generateSecret(spec)
                            .getEncoded()
            );
        }
        catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean test(String password, String hash, String salt) {
        String candidateHash = hash(password, salt);
        return Objects.equals(candidateHash, hash);
    }
}
