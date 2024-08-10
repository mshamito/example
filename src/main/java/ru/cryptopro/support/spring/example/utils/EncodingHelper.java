package ru.cryptopro.support.spring.example.utils;

import ru.cryptopro.support.spring.example.expection.ProvidedDataException;

import java.io.*;
import java.util.Base64;

public class EncodingHelper {
    public static OutputStream encodeStream(OutputStream outputStream) {
        return Base64.getMimeEncoder().wrap(outputStream);
    }

    public static InputStream decodeDerOrB64Stream(InputStream inputStream) throws IOException {
        int firstByteCount = 2;
        byte[] firstBytes = new byte[firstByteCount];
        if (inputStream == null)
            throw new NullPointerException("InputStream is null");
        int read = inputStream.read(firstBytes);
        if (read != firstByteCount)
            throw new ProvidedDataException("Bad Data");
        InputStream mergedStream = new SequenceInputStream(new ByteArrayInputStream(firstBytes), inputStream);
        if (firstBytes[0] == 0x4d && firstBytes[1] == 0x49) {
            //base64 stream decode
            return Base64.getMimeDecoder().wrap(mergedStream);
        }
        return mergedStream;
    }
}
