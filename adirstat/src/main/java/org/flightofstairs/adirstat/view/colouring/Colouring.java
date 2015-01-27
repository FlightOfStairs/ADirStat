package org.flightofstairs.adirstat.view.colouring;

import android.webkit.MimeTypeMap;
import com.google.common.base.Optional;
import com.google.common.base.Verify;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.LTGRAY;
import static java.net.URLConnection.guessContentTypeFromName;
import static java.net.URLConnection.guessContentTypeFromStream;

public final class Colouring {
    @SneakyThrows
    public static int getColour(File file) {
        Verify.verify(file.exists());

        if (!file.isFile()) return LTGRAY;

        Optional<String> possibleMimeType = Optional.fromNullable(guessContentTypeFromName(file.getAbsolutePath()));

        if (! possibleMimeType.isPresent()) {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                possibleMimeType = Optional.fromNullable(guessContentTypeFromStream(inputStream));
            }
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toURL().toString());
        if (!Objects.equals(extension, "") && ! possibleMimeType.isPresent()) {
            possibleMimeType = Optional.fromNullable(extension);
        }

        return possibleMimeType.transform((mimetype) -> HSVToColor(new float[] {(mimetype.hashCode() % 36) * 10, 1, 1})).or(LTGRAY);
    }

    private Colouring() { }
}
