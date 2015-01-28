package org.flightofstairs.adirstat.view.colouring;

import android.webkit.MimeTypeMap;
import com.google.common.base.Optional;
import com.google.common.base.Verify;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Objects;

import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.LTGRAY;
import static org.flightofstairs.adirstat.FileUtils.getMimeType;

public final class Colouring {
    @SneakyThrows
    public static int getColour(File file) {
        Verify.verify(file.exists());

        if (!file.isFile()) return LTGRAY;

        Optional<String> possibleMimeType = getMimeType(file);

        String extension = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toURL().toString());
        if (!Objects.equals(extension, "") && ! possibleMimeType.isPresent()) {
            possibleMimeType = Optional.fromNullable(extension);
        }

        return possibleMimeType.transform((mimetype) -> HSVToColor(new float[] {(mimetype.hashCode() % 36) * 10, 1, 1})).or(LTGRAY);
    }

    private Colouring() { }
}
