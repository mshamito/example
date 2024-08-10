package ru.cryptopro.support.spring.example.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

public class EncodingHelper {
    public static OutputStream encodeStream(OutputStream outputStream) {
        return Base64.getEncoder().wrap(outputStream);
    }
}
