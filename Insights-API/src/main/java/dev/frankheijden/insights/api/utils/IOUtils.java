package dev.frankheijden.insights.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class IOUtils {

    private IOUtils() {}

    /**
     * Copies the contents of a folder in the jar to a target folder.
     */
    public static void copyResources(
            Path target,
            ClassLoader loader,
            Collection<? extends String> collection
    ) {
        try {
            for (String fileName : collection) {
                try (InputStream in = getResource(fileName, loader)) {
                    if (in == null) continue;
                    save(in, target.resolve(fileName));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves a resource from the ClassLoader object.
     */
    public static InputStream getResource(String path, ClassLoader loader) {
        try {
            URL url = loader.getResource(path);
            if (url == null) return null;

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Saves an InputStream to a file. Closes the stream after use.
     */
    public static void save(InputStream in, Path target) throws IOException {
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        in.close();

        Files.write(target, buffer);
    }
}
