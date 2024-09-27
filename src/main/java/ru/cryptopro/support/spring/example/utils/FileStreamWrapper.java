package ru.cryptopro.support.spring.example.utils;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;

@Log4j2
public class FileStreamWrapper{
    private final File file;
    private final static int BUFFER_SIZE = 2 * 1024 * 1024;

    public FileStreamWrapper(@NotNull File file) {
        this.file = file;
    }

    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(file.toPath());
    }

    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(file.toPath());
    }

    public void writeToAndDelete(OutputStream outputStream) throws IOException {
        InputStream inputStream = getInputStream();
        int read;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((read = inputStream.read(buffer)) != -1)
            outputStream.write(buffer, 0, read);
        deleteFile();
    }

    public void deleteFile() {
        String fullPath = file.getAbsoluteFile().toString();
        if (file.delete())
            log.info("temporary file deleted: {}", fullPath);
        else
            log.info("temporary file was not deleted: {}", fullPath);
    }
}
