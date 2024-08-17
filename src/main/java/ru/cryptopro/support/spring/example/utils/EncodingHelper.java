package ru.cryptopro.support.spring.example.utils;

import java.io.*;
import java.util.Base64;

public class EncodingHelper {
    public static OutputStream encodeStream(OutputStream outputStream) {
        return Base64.getMimeEncoder().wrap(outputStream);
    }

    public static byte[] encode(byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }

    public static byte[] decode(String string) {
        return Base64.getDecoder().decode(string);
    }

    public static InputStream decodeDerOrB64Stream(InputStream inputStream) throws IOException {
        int firstByteCount = 2;
        byte[] firstBytes = new byte[firstByteCount];
        if (inputStream == null)
            throw new NullPointerException("InputStream is null");
        int read = inputStream.read(firstBytes);
        if (read != firstByteCount)
            throw new IOException("Bad data in InputStream");
        InputStream mergedStream = new SequenceInputStream(new ByteArrayInputStream(firstBytes), inputStream);
        if (firstBytes[0] == 0x4d && firstBytes[1] == 0x49) {
            //base64 stream decode
            try {
                return Base64.getMimeDecoder().wrap(mergedStream);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("IOException while decoding Base64 stream: " + e.getMessage());
            }
        }
        // return original stream
        return mergedStream;
    }
}
