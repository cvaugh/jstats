package dev.cvaugh.jstats;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class Main {
    private static final String TRUNCATED_CELL =
            "<span class=\"truncated\" title=\"%s\">%s<span class=\"truncation-marker\">&raquo;</span></span>";
    private static final String TIME_ROW =
            "<tr><td>%s</td><td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    private static final String USERS_ROW =
            "<tr><td class=\"left\">%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    private static final String IP_ROW =
            "<tr><td class=\"left\">%s</td><td><a href=\"%s\">View</a></td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    private static final String USER_AGENTS_ROW =
            "<tr><td class=\"left\">%s</td><td>%d</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    private static final String FILES_ROW =
            "<tr><td class=\"left\">%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    private static final String QUERIES_ROW =
            "<tr><td class=\"left\">%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    private static final String RESPONSES_ROW =
            "<tr><td>%d</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    private static final String TIME_TAKEN_ROW = "<tr><td>%s%s%s</td><td>%d</td><td>%s</td></tr>\n";

    public static void main(String[] args) {
        try {
            Config.load();
        } catch(IOException e) {
            Logger.log("Failed to load configuration file from %s", Logger.ERROR,
                    Config.FILE.getAbsolutePath());
            Logger.log(e, Logger.ERROR);
            System.exit(1);
        }
        List<LogEntry> entries = new ArrayList<>();
        String quotedName = Pattern.quote(Config.instance.accessLogName);
        String logRegex = "^" + quotedName + "\\.[0-9]+$";
        String gzipRegex = "^" + quotedName + "\\.[0-9]+\\.gz$";
        File[] files = Config.getAccessLogDirectory().listFiles(file -> {
            if(file.getName().equals(Config.instance.accessLogName))
                return true;
            if(Config.instance.readRotatedLogs) {
                return file.getName().matches(logRegex) || file.getName().matches(gzipRegex);
            }
            return false;
        });
        if(files == null) {
            Logger.log("No logs found", Logger.INFO);
            System.exit(0);
        }
        for(File file : files) {
            entries.addAll(Utils.readLog(file));
        }
        Logger.log("Found %d log entries", Logger.INFO, entries.size());
        Logger.log("Processing log entries", Logger.DEBUG);
        List<LogEntry> remove = new ArrayList<>();
        for(LogEntry entry : entries) {
            entry.process();
            if(Config.instance.ignoreInternalLogs && (entry.remoteHostname.startsWith("127.") ||
                    entry.remoteHostname.equals("::1"))) {
                remove.add(entry);
            }
        }
        if(remove.size() > 0) {
            Logger.log("Removing %d internal requests", Logger.INFO, remove.size());
            entries.removeAll(remove);
        }
        Collections.sort(entries);
        generateStatistics(entries);
    }

    public static void generateStatistics(List<LogEntry> entries) {
        Set<LogElement> availableElements = new HashSet<>(Arrays.stream(LogElement.values())
                .filter(e -> Config.instance.logFormat.contains(e.format)).toList());
        List<OutputSection> availableSections =
                Arrays.stream(OutputSection.values()).filter(s -> s.isUsable(availableElements))
                        .toList();
        String template = "";
        try {
            template = Utils.readTemplate("main");
        } catch(IOException e) {
            Logger.log("Failed to read template: main", Logger.ERROR);
            Logger.log(e, Logger.ERROR);
            System.exit(1);
        }
        for(OutputSection section : availableSections) {
            Logger.log("Generating statistics for %s", Logger.DEBUG,
                    section.toString().toLowerCase());
            try {
                template = template.replace("{{" + section.name().toLowerCase() + "}}",
                        generateOutputSection(section, entries));
            } catch(IOException e) {
                Logger.log("Failed to read template: %s", Logger.ERROR,
                        section.name().toLowerCase());
                Logger.log(e, Logger.ERROR);
                System.exit(1);
            }
        }
        // TODO remove unavailable sections
        Logger.log("Writing %s to %s", Logger.INFO,
                Utils.humanReadableSize(template.getBytes(StandardCharsets.UTF_8).length),
                Config.getOutputFile().getAbsolutePath());
        try {
            Files.writeString(Config.getOutputFile().toPath(), template);
        } catch(IOException e) {
            Logger.log("Failed to write output to %s", Logger.ERROR,
                    Config.getOutputFile().getAbsolutePath());
            Logger.log(e, Logger.ERROR);
        }
    }

    public static String generateOutputSection(OutputSection section, List<LogEntry> entries)
            throws IOException {
        String template = Utils.readTemplate(section);
        switch(section) {
        case GENERATED_DATE -> {
            return Config.getOutputDateFormat().format(System.currentTimeMillis());
        }
        case HEADER -> {
            return template.replace("{{first_visit}}",
                            Config.getOutputDateFormat().format(entries.get(0).time))
                    .replace("{{latest_visit}}", Config.getOutputDateFormat()
                            .format(entries.get(entries.size() - 1).time));
        }
        case OVERALL -> {
            HashSet<String> visitors =
                    new HashSet<>(entries.stream().map(e -> e.remoteHostname).toList());
            return template.replace("{{visitors}}", String.valueOf(visitors.size()))
                    .replace("{{visits}}", String.valueOf(entries.size())).replace("{{bandwidth}}",
                            Utils.humanReadableSize(
                                    entries.stream().mapToLong(e -> e.bytesSent).sum()));
        }
        // TODO handle empty or "-" values
        case YEARLY_TABLE -> {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Long> sizes = new HashMap<>();
            Map<String, HashSet<String>> unique = new HashMap<>();
            long totalBandwidth = 0;
            for(LogEntry entry : entries) {
                String year = Utils.YEAR_FORMAT.format(entry.time);
                counts.put(year, counts.getOrDefault(year, 0) + 1);
                sizes.put(year, sizes.getOrDefault(year, 0L) + entry.bytesSent);
                totalBandwidth += entry.bytesSent;
                unique.computeIfAbsent(year, k -> new HashSet<>());
                unique.get(year).add(entry.remoteHostname);
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(String year : new TreeSet<>(counts.keySet())) {
                sb.append(String.format(TIME_ROW, year,
                        unique.containsKey(year) ? unique.get(year).size() : 0, counts.get(year),
                        Utils.formatPercent(counts.get(year), entries.size()),
                        Utils.humanReadableSize(sizes.get(year)),
                        Utils.formatPercent(sizes.get(year), totalBandwidth)));
            }
            return template.replace("{{rows}}", sb.toString()).replace("{{avg_visitors}}",
                            String.valueOf(
                                    unique.values().stream().mapToInt(Set::size).sum() / counts.size()))
                    .replace("{{avg_visits}}", String.valueOf(entries.size() / counts.size()))
                    .replace("{{avg_bandwidth}}",
                            Utils.humanReadableSize(totalBandwidth / counts.size()));
        }
        case MONTHLY_TABLE -> {
            int[] counts = new int[12];
            long[] sizes = new long[12];
            Map<Integer, HashSet<String>> unique = new HashMap<>();
            long totalBandwidth = 0;
            for(LogEntry entry : entries) {
                int month = Integer.parseInt(Utils.MONTH_FORMAT.format(entry.time)) - 1;
                counts[month]++;
                sizes[month] += entry.bytesSent;
                totalBandwidth += entry.bytesSent;
                unique.computeIfAbsent(month, k -> new HashSet<>());
                unique.get(month).add(entry.remoteHostname);
            }
            StringBuilder sb = new StringBuilder();
            for(int month = 0; month < 12; month++) {
                sb.append(String.format(TIME_ROW, Utils.MONTH_NAMES[month],
                        unique.containsKey(month) ? unique.get(month).size() : 0, counts[month],
                        Utils.formatPercent(counts[month], entries.size()),
                        Utils.humanReadableSize(sizes[month]),
                        Utils.formatPercent(sizes[month], totalBandwidth)));
            }
            return template.replace("{{rows}}", sb.toString()).replace("{{avg_visitors}}",
                            String.valueOf(unique.values().stream().mapToInt(Set::size).sum() / 12))
                    .replace("{{avg_visits}}", String.valueOf(entries.size() / 12))
                    .replace("{{avg_bandwidth}}", Utils.humanReadableSize(totalBandwidth / 12));
        }
        case DAY_OF_WEEK_TABLE -> {
            int[] counts = new int[7];
            long[] sizes = new long[7];
            Map<Integer, HashSet<String>> unique = new HashMap<>();
            long totalBandwidth = 0;
            for(LogEntry entry : entries) {
                int day = Integer.parseInt(Utils.DAY_OF_WEEK_FORMAT.format(entry.time)) - 1;
                counts[day]++;
                sizes[day] += entry.bytesSent;
                totalBandwidth += entry.bytesSent;
                unique.computeIfAbsent(day, k -> new HashSet<>());
                unique.get(day).add(entry.remoteHostname);
            }
            StringBuilder sb = new StringBuilder();
            for(int day = 0; day < 7; day++) {
                sb.append(String.format(TIME_ROW, Utils.DAY_NAMES[day],
                        unique.containsKey(day) ? unique.get(day).size() : 0, counts[day],
                        Utils.formatPercent(counts[day], entries.size()),
                        Utils.humanReadableSize(sizes[day]),
                        Utils.formatPercent(sizes[day], totalBandwidth)));
            }
            return template.replace("{{rows}}", sb.toString()).replace("{{avg_visitors}}",
                            String.valueOf(unique.values().stream().mapToInt(Set::size).sum() / 7))
                    .replace("{{avg_visits}}", String.valueOf(entries.size() / 7))
                    .replace("{{avg_bandwidth}}", Utils.humanReadableSize(totalBandwidth / 7));
        }
        case HOURLY_TABLE -> {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Long> sizes = new HashMap<>();
            Map<String, HashSet<String>> unique = new HashMap<>();
            long totalBandwidth = 0;
            for(LogEntry entry : entries) {
                String hour = Utils.HOUR_FORMAT.format(entry.time);
                counts.put(hour, counts.getOrDefault(hour, 0) + 1);
                sizes.put(hour, sizes.getOrDefault(hour, 0L) + entry.bytesSent);
                totalBandwidth += entry.bytesSent;
                unique.computeIfAbsent(hour, k -> new HashSet<>());
                unique.get(hour).add(entry.remoteHostname);
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(String hour : new TreeSet<>(counts.keySet())) {
                sb.append(String.format(TIME_ROW, hour,
                        unique.containsKey(hour) ? unique.get(hour).size() : 0, counts.get(hour),
                        Utils.formatPercent(counts.get(hour), entries.size()),
                        Utils.humanReadableSize(sizes.get(hour)),
                        Utils.formatPercent(sizes.get(hour), totalBandwidth)));
            }
            return template.replace("{{rows}}", sb.toString()).replace("{{avg_visitors}}",
                            String.valueOf(unique.values().stream().mapToInt(Set::size).sum() / 24))
                    .replace("{{avg_visits}}", String.valueOf(entries.size() / 24))
                    .replace("{{avg_bandwidth}}", Utils.humanReadableSize(totalBandwidth / 24));
        }
        case IP_TABLE -> {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Long> sizes = new HashMap<>();
            Map<String, Long> latestVisits = new HashMap<>();
            long total = 0;
            for(LogEntry entry : entries) {
                counts.put(entry.remoteHostname, counts.getOrDefault(entry.remoteHostname, 0) + 1);
                sizes.put(entry.remoteHostname,
                        sizes.getOrDefault(entry.remoteHostname, 0L) + entry.bytesSent);
                total += entry.bytesSent;
                if(entry.time > latestVisits.getOrDefault(entry.remoteHostname, 0L)) {
                    latestVisits.put(entry.remoteHostname, entry.time);
                }
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(String ip : counts.keySet()) {
                sb.append(String.format(IP_ROW, ip.length() > Config.instance.truncateWideColumns ?
                                String.format(TRUNCATED_CELL, ip,
                                        ip.substring(0, Config.instance.truncateWideColumns)) :
                                ip, Config.instance.whoisTool.replace("{{address}}", ip), counts.get(ip),
                        Utils.formatPercent(counts.get(ip), entries.size()),
                        Utils.humanReadableSize(sizes.get(ip)),
                        Utils.formatPercent(sizes.get(ip), total),
                        Config.getOutputDateFormat().format(latestVisits.get(ip))));
            }
            return template.replace("{{rows}}", sb.toString());
        }
        case USERS_TABLE -> {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Long> sizes = new HashMap<>();
            Map<String, Long> latestVisits = new HashMap<>();
            long total = 0;
            for(LogEntry entry : entries) {
                counts.put(entry.remoteUser, counts.getOrDefault(entry.remoteUser, 0) + 1);
                sizes.put(entry.remoteUser,
                        sizes.getOrDefault(entry.remoteUser, 0L) + entry.bytesSent);
                total += entry.bytesSent;
                if(entry.time > latestVisits.getOrDefault(entry.remoteUser, 0L)) {
                    latestVisits.put(entry.remoteUser, entry.time);
                }
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(String user : counts.keySet()) {
                sb.append(String.format(USERS_ROW,
                        user.length() > Config.instance.truncateWideColumns ?
                                String.format(TRUNCATED_CELL, user,
                                        user.substring(0, Config.instance.truncateWideColumns)) :
                                user, counts.get(user),
                        Utils.formatPercent(counts.get(user), entries.size()),
                        Utils.humanReadableSize(sizes.get(user)),
                        Utils.formatPercent(sizes.get(user), total),
                        Config.getOutputDateFormat().format(latestVisits.get(user))));
            }
            return template.replace("{{rows}}", sb.toString());
        }
        case USER_AGENT_TABLE -> {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Long> sizes = new HashMap<>();
            Map<String, Long> latestVisits = new HashMap<>();
            Map<String, HashSet<String>> unique = new HashMap<>();
            long total = 0;
            for(LogEntry entry : entries) {
                counts.put(entry.userAgent, counts.getOrDefault(entry.userAgent, 0) + 1);
                sizes.put(entry.userAgent,
                        sizes.getOrDefault(entry.userAgent, 0L) + entry.bytesSent);
                total += entry.bytesSent;
                if(entry.time > latestVisits.getOrDefault(entry.userAgent, 0L)) {
                    latestVisits.put(entry.userAgent, entry.time);
                }
                unique.computeIfAbsent(entry.userAgent, k -> new HashSet<>());
                unique.get(entry.userAgent).add(entry.remoteHostname);
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(String userAgent : counts.keySet()) {
                sb.append(String.format(USER_AGENTS_ROW,
                        userAgent.length() > Config.instance.truncateWideColumns ?
                                String.format(TRUNCATED_CELL, userAgent, userAgent.substring(0,
                                        Config.instance.truncateWideColumns)) :
                                userAgent,
                        unique.containsKey(userAgent) ? unique.get(userAgent).size() : 0,
                        counts.get(userAgent),
                        Utils.formatPercent(counts.get(userAgent), entries.size()),
                        Utils.humanReadableSize(sizes.get(userAgent)),
                        Utils.formatPercent(sizes.get(userAgent), total),
                        Config.getOutputDateFormat().format(latestVisits.get(userAgent))));
            }
            return template.replace("{{rows}}", sb.toString());
        }
        case FILES_TABLE -> {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Long> sizes = new HashMap<>();
            Map<String, Long> latestVisits = new HashMap<>();
            long total = 0;
            for(LogEntry entry : entries) {
                counts.put(entry.filename, counts.getOrDefault(entry.filename, 0) + 1);
                sizes.put(entry.filename, sizes.getOrDefault(entry.filename, 0L) + entry.bytesSent);
                total += entry.bytesSent;
                if(entry.time > latestVisits.getOrDefault(entry.filename, 0L)) {
                    latestVisits.put(entry.filename, entry.time);
                }
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(String filename : counts.keySet()) {
                sb.append(String.format(FILES_ROW,
                        filename.length() > Config.instance.truncateWideColumns ?
                                String.format(TRUNCATED_CELL, filename, filename.substring(0,
                                        Config.instance.truncateWideColumns)) :
                                filename, counts.get(filename),
                        Utils.formatPercent(counts.get(filename), entries.size()),
                        Utils.humanReadableSize(sizes.get(filename)),
                        Utils.formatPercent(sizes.get(filename), total),
                        Utils.humanReadableSize(sizes.get(filename) / counts.get(filename)),
                        Config.getOutputDateFormat().format(latestVisits.get(filename))));
            }
            return template.replace("{{rows}}", sb.toString());
        }
        case QUERIES_TABLE -> {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Long> sizes = new HashMap<>();
            long total = 0;
            for(LogEntry entry : entries) {
                counts.put(entry.query, counts.getOrDefault(entry.query, 0) + 1);
                sizes.put(entry.query, sizes.getOrDefault(entry.query, 0L) + entry.bytesSent);
                total += entry.bytesSent;
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(String query : counts.keySet()) {
                sb.append(String.format(QUERIES_ROW,
                        query.length() > Config.instance.truncateWideColumns ?
                                String.format(TRUNCATED_CELL, query,
                                        query.substring(0, Config.instance.truncateWideColumns)) :
                                query, counts.get(query),
                        Utils.formatPercent(counts.get(query), entries.size()),
                        Utils.humanReadableSize(sizes.get(query)),
                        Utils.formatPercent(sizes.get(query), total)));
            }
            return template.replace("{{rows}}", sb.toString());
        }
        case REFERERS_TABLE -> {
            Map<String, Integer> counts = new HashMap<>();
            Map<String, Long> sizes = new HashMap<>();
            long total = 0;
            for(LogEntry entry : entries) {
                counts.put(entry.referer, counts.getOrDefault(entry.referer, 0) + 1);
                sizes.put(entry.referer, sizes.getOrDefault(entry.referer, 0L) + entry.bytesSent);
                total += entry.bytesSent;
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(String referer : counts.keySet()) {
                sb.append(String.format(QUERIES_ROW,
                        referer.length() > Config.instance.truncateWideColumns ?
                                String.format(TRUNCATED_CELL, referer,
                                        referer.substring(0, Config.instance.truncateWideColumns)) :
                                referer, counts.get(referer),
                        Utils.formatPercent(counts.get(referer), entries.size()),
                        Utils.humanReadableSize(sizes.get(referer)),
                        Utils.formatPercent(sizes.get(referer), total)));
            }
            return template.replace("{{rows}}", sb.toString());
        }
        case RESPONSES_TABLE -> {
            Map<Integer, Integer> counts = new HashMap<>();
            Map<Integer, Long> sizes = new HashMap<>();
            long total = 0;
            for(LogEntry entry : entries) {
                counts.put(entry.statusFinal, counts.getOrDefault(entry.statusFinal, 0) + 1);
                sizes.put(entry.statusFinal,
                        sizes.getOrDefault(entry.statusFinal, 0L) + entry.bytesSent);
                total += entry.bytesSent;
            }
            counts = Utils.sortByValue(counts);
            StringBuilder sb = new StringBuilder();
            for(int response : counts.keySet()) {
                sb.append(String.format(RESPONSES_ROW, response, counts.get(response),
                        Utils.formatPercent(counts.get(response), entries.size()),
                        Utils.humanReadableSize(sizes.get(response)),
                        Utils.formatPercent(sizes.get(response), total)));
            }
            return template.replace("{{rows}}", sb.toString());
        }
        case TIME_TAKEN_TABLE -> {
            int[] buckets = new int[Utils.TIME_TAKEN_BUCKETS.length + 1];
            long total = 0;
            for(LogEntry entry : entries) {
                total += entry.timeToServeUs;
                if(entry.timeToServeUs >=
                        Utils.TIME_TAKEN_BUCKETS[Utils.TIME_TAKEN_BUCKETS.length - 1]) {
                    buckets[Utils.TIME_TAKEN_BUCKETS.length]++;
                } else {
                    for(int i = 0; i < Utils.TIME_TAKEN_BUCKETS.length; i++) {
                        if(entry.timeToServeUs < Utils.TIME_TAKEN_BUCKETS[i]) {
                            buckets[i]++;
                            break;
                        }
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(TIME_TAKEN_ROW, "", "&lt; ", Utils.TIME_TAKEN_BUCKETS[0],
                    buckets[0], Utils.formatPercent(buckets[0], entries.size())));
            for(int i = 1; i < buckets.length; i++) {
                long size = buckets[i];
                if(i == Utils.TIME_TAKEN_BUCKETS.length) {
                    sb.append(String.format(TIME_TAKEN_ROW, "", "&geq; ",
                            Utils.TIME_TAKEN_BUCKETS[i - 1], size,
                            Utils.formatPercent(size, entries.size())));
                } else {
                    sb.append(String.format(TIME_TAKEN_ROW, Utils.TIME_TAKEN_BUCKETS[i - 1], "-",
                            (Utils.TIME_TAKEN_BUCKETS[i] - 1), size,
                            Utils.formatPercent(size, entries.size())));
                }
            }
            return template.replace("{{rows}}", sb.toString())
                    .replace("{{avg}}", String.valueOf(total / entries.size()));
        }
        case FOOTER -> {
            return template;
        }
        }
        return "?";
    }
}
