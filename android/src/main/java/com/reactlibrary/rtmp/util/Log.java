package com.reactlibrary.rtmp.util;

/**
 * Created by jsyan on 16-12-9.
 */

public class Log {
    public static void d(String tag, String message) {
        System.out.println(tag + message);
    }

    public static void w(String tag, String message) {
        System.out.println(tag + message);
    }

    public static void e(String tag, String message) {
        System.out.println(tag + message);
    }
}
