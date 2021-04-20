package com.sgpublic.bilidownload.util;

public class Base64Util {
    public static byte[] Decode(String content) {
        char[] data = content.toCharArray();
        byte[] codes = new byte[256];
        for (int i = 0; i < 256; i++) {
            codes[i] = -1;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            codes[i] = (byte) (i - 'A');
        }

        for (int i = 'a'; i <= 'z'; i++) {
            codes[i] = (byte) (26 + i - 'a');
        }
        for (int i = '0'; i <= '9'; i++) {
            codes[i] = (byte) (52 + i - '0');
        }
        codes['+'] = 62;
        codes['/'] = 63;

        int tempLen = data.length;
        for (char datum : data) {
            if ((datum > 255) || codes[datum] < 0) {
                --tempLen;
            }
        }

        int len = (tempLen / 4) * 3;
        if ((tempLen % 4) == 3) {
            len += 2;
        }
        if ((tempLen % 4) == 2) {
            len += 1;

        }
        byte[] out = new byte[len];

        int shift = 0;
        int accum = 0;
        int index = 0;

        for (char datum : data) {
            int value = (datum > 255) ? -1 : codes[datum];

            if (value >= 0) {
                accum <<= 6;
                shift += 6;
                accum |= value; // at the bottom.
                if (shift >= 8) {
                    shift -= 8;
                    out[index++] = (byte) ((accum >> shift) & 0xff);
                }
            }
        }

        if (index != out.length) {
            return new byte[0];
        } else {
            return out;
        }
    }

    public static String Encode(byte[] content) {
        final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
        char[] out = new char[((content.length + 2) / 3) * 4];
        for (int i = 0, index = 0; i < content.length; i += 3, index += 4) {
            boolean quad = false;
            boolean trip = false;

            int val = (0xFF & (int) content[i]);
            val <<= 8;
            if ((i + 1) < content.length) {
                val |= (0xFF & (int) content[i + 1]);
                trip = true;
            }
            val <<= 8;
            if ((i + 2) < content.length) {
                val |= (0xFF & (int) content[i + 2]);
                quad = true;
            }
            out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 1] = alphabet[val & 0x3F];
            val >>= 6;
            out[index] = alphabet[val & 0x3F];
        }
        return new String(out);
    }
}
