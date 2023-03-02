package dev.cvaugh.jstats;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Utils {
    public static final Gson GSON = new Gson();
    public static final String[] DAY_NAMES =
            new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    public static final String[] MONTH_NAMES =
            new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
                    "Nov", "Dec" };
    public static final DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
    public static final DateFormat MONTH_FORMAT = new SimpleDateFormat("M");
    public static final DateFormat DAY_OF_WEEK_FORMAT = new SimpleDateFormat("u");
    public static final DateFormat HOUR_FORMAT = new SimpleDateFormat("HH");
    private static final DecimalFormat BYTES_FORMAT = new DecimalFormat("#.##");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00%");
    private static Pattern logPattern;

    public static List<LogEntry> readLog(File file) {
        Logger.log("Reading log file: %s", Logger.DEBUG, file.getAbsolutePath());
        List<String> lines;
        try {
            if(file.getName().endsWith(".gz")) {
                lines = new ArrayList<>();
                InputStream fis = new FileInputStream(file);
                InputStream gis = new GZIPInputStream(fis);
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
                String line;
                while((line = br.readLine()) != null) {
                    lines.add(line);
                }
                gis.close();
                fis.close();
            } else {
                lines = Files.readAllLines(file.toPath());
            }
        } catch(Exception e) {
            Logger.log("Failed to read log: %s", Logger.ERROR, file.getAbsolutePath());
            Logger.log(e, Logger.ERROR);
            return new ArrayList<>();
        }
        return Utils.parseLog(lines);
    }

    public static List<LogEntry> parseLog(List<String> lines) {
        int skipped = 0;
        logPattern = Pattern.compile(createFormatRegex(Config.instance.logFormat));
        List<LogEntry> entries = new ArrayList<>();
        for(String line : lines) {
            LogEntry entry = parseLogLine(line);
            if(entry == null) {
                skipped++;
            } else {
                entries.add(entry);
            }
        }
        if(Config.instance.printMalformedEntries)
            Logger.log("Skipped %d malformed entries", Logger.DEBUG, skipped);
        return entries;
    }

    public static LogEntry parseLogLine(String line) {
        Matcher matcher = logPattern.matcher(line);
        if(!matcher.find()) {
            return null;
        }
        Map<LogElement, String> values = new EnumMap<>(LogElement.class);
        for(LogElement e : LogElement.values()) {
            try {
                values.put(e, matcher.group(e.groupName));
            } catch(IllegalArgumentException ignore) {}
        }
        return new LogEntry(values);
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

    public static String readTemplate(String name) throws IOException {
        InputStream in = Utils.class.getResourceAsStream("/templates/" + name + ".html");
        if(in == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String template = String.join("\n", reader.lines().toList());
        reader.close();
        in.close();
        return template;
    }

    public static String readTemplate(OutputSection section) throws IOException {
        return readTemplate(section.name().toLowerCase());
    }

    public static String humanReadableSize(long bytes) {
        double b = (double) bytes;
        int magnitude = 0;
        while(b > 1000) {
            b /= 1000;
            magnitude++;
        }
        if(magnitude == 0) {
            return b + " B";
        } else {
            return String.format("%s %sB", BYTES_FORMAT.format(b),
                    "kMGTPEZY".charAt(magnitude - 1));
        }
    }

    public static String formatPercent(long part, long whole) {
        return PERCENT_FORMAT.format((double) part / (double) whole);
    }

    // https://stackoverflow.com/a/2581754
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        Map<K, V> result = new LinkedHashMap<>();
        for(Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
