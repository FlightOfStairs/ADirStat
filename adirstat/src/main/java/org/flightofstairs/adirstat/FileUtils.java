package org.flightofstairs.adirstat;

import com.google.common.base.Optional;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static java.net.URLConnection.guessContentTypeFromName;
import static java.net.URLConnection.guessContentTypeFromStream;

public final class FileUtils {

    public static final String DIRECTORY_MIMETYPE = "inode/directory";

    @SneakyThrows
    public static Optional<String> getMimeType(@Nonnull File file) {
        if (file.isDirectory()) return Optional.of(DIRECTORY_MIMETYPE);

        try {
            Optional<String> possibleMimeType = Optional.fromNullable(guessContentTypeFromName(file.getAbsolutePath()));

            if (!possibleMimeType.isPresent()) {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                    possibleMimeType = Optional.fromNullable(guessContentTypeFromStream(inputStream));
                } catch (IOException e) {
                    return Optional.absent();
                } finally {
                    if (inputStream != null) inputStream.close();
                }
            }
            return possibleMimeType;
        } catch (StringIndexOutOfBoundsException ignored) {
            return Optional.absent();
        }
    }

    private FileUtils() { }
}
