package dev.cvaugh.jstats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) {
        try {
            Config.load();
        } catch(IOException e) {
            System.err.println(
                    "Failed to load configuration file from " + Config.FILE.getAbsolutePath());
            e.printStackTrace();
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
