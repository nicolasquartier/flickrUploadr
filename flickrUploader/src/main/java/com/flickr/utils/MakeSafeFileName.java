package com.flickr.utils;

public class MakeSafeFileName {


    private static boolean replaceSpaces = false;

    public static String makeSafeFilename(String input) {
        byte[] fname = input.getBytes();
        byte[] bad = new byte[] { '\\', '/', '"', '*' };
        byte replace = '_';
        for (int i = 0; i < fname.length; i++) {
            for (byte element : bad) {
                if (fname[i] == element) {
                    fname[i] = replace;
                }
            }
            if (replaceSpaces && fname[i] == ' ')
                fname[i] = '_';
        }
        return new String(fname);
    }
}
