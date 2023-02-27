package dev.cvaugh.jstats;

import com.google.gson.Gson;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final Gson GSON = new Gson();
    private static final Pattern COMBINED_LOG_MATCHER = Pattern.compile(
            LogElement.createRegex("%h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\""));
    private static Pattern logPattern;

    public static void parseLog(List<String> lines) {
        logPattern = Pattern.compile(LogElement.createRegex(Config.instance.logFormat));
        System.out.println(logPattern.pattern());
        System.out.println();
        System.out.println(COMBINED_LOG_MATCHER.pattern());
        for(String line : lines) {
            parseLogLine(line);
        }
    }

    public static void parseLogLine(String line) {
        Matcher matcher = logPattern.matcher(line);
        if(matcher.find()) {
            // TODO
        } else if(Config.instance.allowFallback) {
            Matcher fallback = COMBINED_LOG_MATCHER.matcher(line);
            if(fallback.find()) {
                // TODO
            } else {
                // System.err.println("Not matched");
            }
        } else {
            // System.err.println("Not matched");
        }
        //System.exit(0); // XXX
    }

    public static String replaceTildeInPath(String path) {
        return path.replaceFirst("^~", System.getProperty("user.home").replaceAll("\\\\", "/"));
    }
}
