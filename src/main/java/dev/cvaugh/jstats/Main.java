package dev.cvaugh.jstats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Main {

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
        Logger.log("Generating statistics", Logger.DEBUG);
        Set<LogElement> availableElements = new HashSet<>(Arrays.stream(LogElement.values())
                .filter(e -> Config.instance.logFormat.contains(e.format)).toList());
        List<OutputSection> availableSections =
                Arrays.stream(OutputSection.values()).filter(s -> s.isUsable(availableElements))
                        .toList();
        String template = "";
        try {
            template = Utils.readTemplate("main");
        } catch(Exception e) {
            Logger.log("Failed to read template", Logger.ERROR);
            Logger.log(e, Logger.ERROR);
            System.exit(1);
        }
        for(OutputSection section : availableSections) {
            template = template.replace("{{" + section.name().toLowerCase() + "}}",
                    generateOutputSection(section, entries));
        }
        Logger.log("Writing output to %s", Logger.INFO, Config.getOutputFile().getAbsolutePath());
        try {
            Files.writeString(Config.getOutputFile().toPath(), template);
        } catch(IOException e) {
            Logger.log("Failed to write output to %s", Logger.ERROR,
                    Config.getOutputFile().getAbsolutePath());
            Logger.log(e, Logger.ERROR);
        }
    }

    public static String generateOutputSection(OutputSection section, List<LogEntry> entries) {
        switch(section) {
        case GENERATED_DATE -> {
            return Config.getOutputDateFormat().format(System.currentTimeMillis());
        }
        case HEADER -> {}
        case OVERALL -> {}
        case YEARLY_TABLE -> {}
        case MONTHLY_TABLE -> {}
        case DAY_OF_MONTH_TABLE -> {}
        case DAY_OF_WEEK_TABLE -> {}
        case HOURLY_TABLE -> {}
        case IP_TABLE -> {}
        case USERS_TABLE -> {}
        case USER_AGENT_TABLE -> {}
        case PAGES_TABLE -> {}
        case FILES_TABLE -> {}
        case QUERIES_TABLE -> {}
        case REFERERS_TABLE -> {}
        case RESPONSES_TABLE -> {}
        case TIME_TAKEN_TABLE -> {}
        case FOOTER -> {}
        }
        return "?";
    }
}
