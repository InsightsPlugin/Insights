package dev.frankheijden.insights.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtils {

    private IOUtils() {}

    /**
     * Constructs a new FileSystem using given classloader.
     */
    public static FileSystem getFileSystem(ClassLoader loader) throws IOException {
        URL jar = IOUtils.class.getProtectionDomain().getCodeSource().getLocation();
        Path jarFile = Paths.get(jar.toString().substring(5));
        return FileSystems.newFileSystem(jarFile, loader);
    }

    /**
     * Copies a whole folder from the jar file to given path target.
     */
    public static void copyResourceFolder(String folder, Path target, ClassLoader loader) {
        try (
                FileSystem fs = IOUtils.getFileSystem(loader);
                DirectoryStream<Path> paths = Files.newDirectoryStream(fs.getPath(folder))
        ) {
            for (Path path : paths) {
                try (InputStream in = loader.getResourceAsStream(path.toString())) {
                    if (in == null) continue;
                    save(in, target.resolve(path.getFileName().toString()));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
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
