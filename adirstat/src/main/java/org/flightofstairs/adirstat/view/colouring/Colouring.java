package org.flightofstairs.adirstat.view.colouring;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Verify;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;

import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.LTGRAY;
import static java.net.URLConnection.guessContentTypeFromName;
import static java.net.URLConnection.guessContentTypeFromStream;

public enum Colouring implements Function<File, Integer> {
    BASIC;

    @SneakyThrows
    @Nullable
    @Override
    public Integer apply(File file) {
        Verify.verify(file.exists());

        if (!file.isFile()) return LTGRAY;

        Optional<String> possibleMimeType = Optional.fromNullable(guessContentTypeFromName(file.getAbsolutePath()));

        if (! possibleMimeType.isPresent()) {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                possibleMimeType = Optional.fromNullable(guessContentTypeFromStream(inputStream));
            }
        }

        return possibleMimeType.transform((mimetype) -> HSVToColor(new float[] {(mimetype.hashCode() % 36) * 10, 1, 1})).or(LTGRAY);
    }

}
