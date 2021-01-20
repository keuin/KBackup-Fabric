package com.keuin.kbackupfabric.util;

import java.nio.charset.StandardCharsets;

public class BytesUtil {
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8).toUpperCase();
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        if (len % 2 != 0)
            throw new IllegalArgumentException("Invalid hex string.");
        byte[] b = new byte[len / 2];
        int index, v;
        for (int i = 0; i < b.length; i++) {
            index = i * 2;
            v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}
