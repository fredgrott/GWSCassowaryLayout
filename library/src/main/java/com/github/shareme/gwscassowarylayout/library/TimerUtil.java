package com.github.shareme.gwscassowarylayout.library;

/**
 * Created by alex on 08/09/2014.
 */
public class TimerUtil {
    public static long since(long since) {
        return (System.nanoTime() - since) / 1000000;
    }
}
