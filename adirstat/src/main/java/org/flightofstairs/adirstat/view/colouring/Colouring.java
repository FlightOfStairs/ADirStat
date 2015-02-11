package org.flightofstairs.adirstat.view.colouring;

import android.webkit.MimeTypeMap;
import com.google.common.base.Optional;
import com.google.common.base.Verify;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Objects;

import static android.graphics.Color.*;
import static java.lang.Math.abs;
import static org.flightofstairs.adirstat.FileUtils.getMimeType;

public final class Colouring {

    @SneakyThrows
    public static int getColour(File file) {
        Verify.verify(file.exists());

        if (!file.isFile()) return LTGRAY;

        Optional<String> possibleMimeType = getMimeType(file);
        if (possibleMimeType.isPresent()) return colourMime(possibleMimeType.get());

        String extension = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toURL().toString());
        if (!Objects.equals(extension, "")) return fallback(extension);

        return fallback(file.getName());
    }

    private static int colourMime(String mimeType) {
        String[] split = mimeType.split("/");
        String type = "application".equals(split[0]) && split.length > 1 ? split[1] : split[0];

        switch (type) {
            case "image": return RED;
            case "audio": return BLUE;
            case "video": return GREEN;
            case "application": return YELLOW;
            case "text": return LTGRAY;
            case "zip": return CYAN;
            case "pdf": return MAGENTA;

            default:
                System.out.println(type);
                return fallback(type);
        }
    }

    private static int fallback(String mimeType) {
        return HSVToColor(new float[]{(abs(("a random string" + mimeType).hashCode()) % 36) * 10, 1, 1});
    }

    private Colouring() { }
}
