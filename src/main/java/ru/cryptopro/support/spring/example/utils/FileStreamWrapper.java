package ru.cryptopro.support.spring.example.utils;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Log4j2
public class FileStreamWrapper {
    private final File file;
    private final static int BUFFER_SIZE = 2 * 1024 * 1024;

    public FileStreamWrapper(@NotNull File file) {
        this.file = file;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try (InputStream inputStream = getInputStream()) {
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, read);
        }
    }

    public OutputStream getOutputStream() throws IOException {
        validateFile();
        log.info("temporary file was opened for writing: {}", file.getAbsolutePath());
        return Files.newOutputStream(file.toPath());
    }

    public InputStream getInputStream() throws IOException {
        validateFile();
        log.info("temporary file was opened for reading, will be destroyed after reading: {}", file.getAbsolutePath());
        return Files.newInputStream(file.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
    }

    private void validateFile() {
        if (!file.exists())
            log.error("temporary file not exists: {}", file.getAbsolutePath());
        else if (!file.canWrite())
            log.error("incorrect file permission: {}", file.getAbsolutePath());
    }
}
