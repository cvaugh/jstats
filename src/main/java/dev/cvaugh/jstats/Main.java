package dev.cvaugh.jstats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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
        File logFile = new File(Config.getAccessLogDirectory(), Config.instance.accessLogName);
        Logger.log("Reading logs", Logger.DEBUG);
        List<LogEntry> entries = null;
        try {
            entries = Utils.parseLog(Files.readAllLines(logFile.toPath()));
        } catch(IOException e) {
            Logger.log("Failed to parse log: %s", Logger.ERROR, logFile.getAbsolutePath());
            Logger.log(e, Logger.ERROR);
            System.exit(1);
        }
        Logger.log("Processing log entries", Logger.DEBUG);
        for(LogEntry entry : entries) {
            entry.process();
        }
    }
}
