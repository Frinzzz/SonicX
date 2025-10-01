package game.util;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ResourceUtil {
    private ResourceUtil() {}

    private static String url(String classpathPath) {
        String srcRoot = "src/game";
        String filePath = srcRoot + classpathPath;
        Path p = Paths.get(filePath);
        if (Files.exists(p)) {
            return "file:" + p.toFile().getPath().replace(File.separatorChar, '/');
        }
        URL u = ResourceUtil.class.getResource(classpathPath);
        if (u == null) throw new IllegalArgumentException("Resource not found: " + classpathPath);
        return u.toExternalForm();
    }

    public static Image image(String classpathPath) {
        return new Image(url(classpathPath));
    }

    public static AudioClip clip(String classpathPath) {
        return new AudioClip(url(classpathPath));
    }
}
