package dev.cvaugh.jstats;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class Logger {
    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;
    public static final int NONE = 4;

    private static void log(String s, String prefix, PrintStream stream) {
        stream.printf("[%s] %s\n", prefix, s);
    }

    public static void log(String s, int level) {
        if(level == NONE || level < Config.instance.logVerbosity) return;
        switch(level) {
        case DEBUG -> log(s, "DEBUG", System.out);
        case INFO -> log(s, "INFO", System.out);
        case WARN -> log(s, "WARN", System.err);
        case ERROR -> log(s, "ERROR", System.err);
        default -> {}
        }
    }

    public static void log(String s, int level, Object... format) {
        if(level == NONE || level < Config.instance.logVerbosity) return;
        log(String.format(s, format), level);
    }

    public static void log(Throwable t, int level) {
        for(String line : stacktraceToString(t).split("\n")) {
            log(line, level);
        }
    }

    // Modified from https://stackoverflow.com/a/10120715
    private static String stacktraceToString(Throwable t) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        t.printStackTrace(ps);
        ps.close();
        return os.toString();
    }
}
