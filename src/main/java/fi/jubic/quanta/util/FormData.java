package fi.jubic.quanta.util;

import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

public class FormData {
    public static Optional<String> parseString(Part part) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(
                    part.getInputStream(),
                    StandardCharsets.UTF_8
            );
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                return Optional.of(
                        reader.lines().collect(Collectors.joining("\n"))
                );
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
