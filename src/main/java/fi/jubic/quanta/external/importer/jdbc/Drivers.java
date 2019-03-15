package fi.jubic.quanta.external.importer.jdbc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Drivers {
    public static Optional<Driver> get(String path, String className) {
        return getClassLoader(path)
                .flatMap(cl -> {
                    try {
                        return Optional.of(
                                (Driver) Class.forName(className, true, cl)
                                        .newInstance()
                        );
                    }
                    catch (InstantiationException
                            | IllegalAccessException
                            | ClassNotFoundException ignored
                    ) {
                    }

                    return Optional.empty();
                })
                .map(DriverShim::of);
    }

    @SuppressFBWarnings(
            value = "PATH_TRAVERSAL_IN",
            justification = "path is jdbcPath from server configurations, it's safe."
    )
    public static List<String> getImplementations(
            String path
    ) {
        List<String> result = new ArrayList<>();
        Optional<ClassLoader> classLoader = getClassLoader(path);

        if (!classLoader.isPresent()) {
            return result;
        }

        try (
                JarInputStream jis = new JarInputStream(
                        new FileInputStream(path)
                )
        ) {
            JarEntry entry = jis.getNextJarEntry();

            while (entry != null) {
                String name = entry.getName();

                if (name.endsWith(".class")) {
                    name = name.replaceAll("/", "\\.");
                    name = name.substring(0, name.length() - 6);

                    if (!name.contains("$")) {
                        try {
                            Class<?> className = Class.forName(
                                    name,
                                    true,
                                    classLoader.get()
                            );

                            if (Driver.class.isAssignableFrom(className)) {
                                result.add(name);
                            }
                        }
                        catch (ClassNotFoundException
                                | NoClassDefFoundError ignored
                        ) {
                        }
                    }
                }

                entry = jis.getNextJarEntry();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static Optional<ClassLoader> getClassLoader(String path) {
        String p = String.format("jar:file:%s!/", path);
        URL url;

        try {
            url = new URL(p);
        }
        catch (MalformedURLException e) {
            return Optional.empty();
        }

        return Optional.of(new URLClassLoader(new URL[] { url }));
    }
}
