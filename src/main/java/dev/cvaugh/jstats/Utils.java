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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Utils {
    public static final Gson GSON = new Gson();
    private static Pattern logPattern;
    private static int skipped = 0;

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
        return Utils.parseLog(lines, file.getName());
    }

    public static List<LogEntry> parseLog(List<String> lines, String fileName) {
        skipped = 0;
        logPattern = Pattern.compile(createFormatRegex(Config.instance.logFormat));
        List<LogEntry> entries = new ArrayList<>();
        int i = 1;
        for(String line : lines) {
            LogEntry entry = parseLogLine(line);
            if(entry == null) {
                skipped++;
            } else {
                entries.add(entry);
            }
            i++;
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
            throw new RuntimeException("Template not found: " + name);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String template = String.join("\n", reader.lines().toList());
        reader.close();
        in.close();
        return template;
    }
}
