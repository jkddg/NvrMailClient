package com.jkddg.nvrmailclient.util;

/**
 * @Author huangyonghao
 * @create 2023/1/11 21:44
 */
public class ByteUtil {
    public static int[] byteArrayToIntList(byte[] b) {
        if (b.length % 4 != 0) {
            return null;
        }
        int[] a = new int[b.length / 4];
        for (int i1 = 0, i2 = 1, i3 = 2, i4 = 3, i5 = 0; i4 < b.length; i1 += 4, i2 += 4, i3 += 4, i4 += 4, i5++) {
            byte[] ab = new byte[4];
            ab[0] = b[i1];
            ab[1] = b[i2];
            ab[2] = b[i3];
            ab[3] = b[i4];
            a[i5] = byteArrayToInt(ab);
        }
        return a;
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }


    public static int[] byteArrayToIntArray(byte[] b) {
        if (b.length == 0) {
            return new int[0];
        }
        int[] result = new int[b.length];
        for (int i = 0; i < b.length; i++) {
            result[i] = b[i];
        }
        return result;
    }

    public static String byteToString(byte[] b,String charsetName){
        String result = "";
        try {
            result = new String(b, charsetName).trim();
        } catch (Exception ex) {

        }
        return result;
    }
}
