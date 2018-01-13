
package com.android.mms.util;


import java.util.concurrent.CopyOnWriteArrayList;

public class ExternalLogger {
    private static final CopyOnWriteArrayList<LoggingListener> sListener = new CopyOnWriteArrayList<LoggingListener>();

    public interface LoggingListener {
        void onLogException(String tag, Throwable e);
        void onLogMessage(String tag, String message);
    }

    private ExternalLogger(){}

    public static void addListener(LoggingListener listener) {
        sListener.add(listener);
    }

    public static void removeListener(LoggingListener listener) {
        sListener.remove(listener);
    }

    public static void logException(String tag, Throwable e) {
        for (LoggingListener listener: sListener) {
            listener.onLogException(tag, e);
        }
    }

    public static void logMessage(String tag, String message) {
        for (LoggingListener listener: sListener) {
            listener.onLogMessage(tag, message);
        }
    }
}
