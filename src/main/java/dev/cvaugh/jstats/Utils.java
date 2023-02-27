package dev.cvaugh.jstats;

import com.google.gson.Gson;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final Gson GSON = new Gson();
    private static final Pattern COMBINED_LOG_MATCHER = Pattern.compile(
            createFormatRegex("%h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\""));
    private static Pattern logPattern;

    public static void parseLog(List<String> lines) {
        logPattern = Pattern.compile(createFormatRegex(Config.instance.logFormat));
        for(String line : lines) {
            Map<LogElement, String> values = parseLogLine(line);
            if(values == null) {
                // TODO print error message
                continue;
            }
            System.out.println(values.keySet());
        }
    }

    public static Map<LogElement, String> parseLogLine(String line) {
        Matcher matcher = logPattern.matcher(line);
        if(!matcher.find()) {
            if(Config.instance.allowFallback) {
                matcher = COMBINED_LOG_MATCHER.matcher(line);
                if(!matcher.find()) {
                    return null;
                }
            } else {
                return null;
            }
        }
        Map<LogElement, String> values = new EnumMap<>(LogElement.class);
        for(LogElement e : LogElement.values()) {
            try {
                values.put(e, matcher.group(e.groupName));
            } catch(IllegalArgumentException ignore) {}
        }
        return values;
    }

    public static String replaceTildeInPath(String path) {
        return path.replaceFirst("^~", System.getProperty("user.home").replaceAll("\\\\", "/"));
    }

    public static String createFormatRegex(String logFormat) {
        for(LogElement e : LogElement.values()) {
            logFormat = logFormat.replaceAll(Pattern.quote(e.format),
                    String.format("(?<%s>%s)", e.groupName, e.regex));
        }
        return "^" + logFormat + "$";
    }
}
