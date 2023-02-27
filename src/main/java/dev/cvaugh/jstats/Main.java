package dev.cvaugh.jstats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        File logFile =
                new File(Config.instance.getAccessLogDirectory(), Config.instance.accessLogName);
        try {
            Utils.parseLog(Files.readAllLines(logFile.toPath()));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
