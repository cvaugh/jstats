package dev.cvaugh.jstats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Config {
    private static final File CONFIG_DIR = new File(
            Utils.replaceTildeInPath("~" + File.separator + ".config" + File.separator + "jstats"));
    public static final File FILE = new File(CONFIG_DIR, "config.json");
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Config instance = new Config();
    private static DateFormat inputDF;
    private static DateFormat outputDF;

    public String accessLogDirectory = "/var/log/apache2";
    public String accessLogName = "access.log";
    public boolean readRotatedLogs = true;
    public String logFormat =
            "%v:%p %h %l %u %t \"%r\" %s:%>s %I %O \"%{Referer}i\" \"%{User-Agent}i\" %D %k %f \"%U\" \"%q\"";
    public String outputDirectory = "~";
    public String outputFileName = "jstats.html";
    public boolean outputMonthSubpages = true;
    public String monthSubpagePattern = "{{year}}-{{month}}.html";
    public String inputDateFormat = "dd/MMM/yyyy:HH:mm:ss Z";
    public String outputDateFormat = "yyyy-MM-dd HH:mm:ss zzz";
    public String whoisTool = "https://iplocation.io/ip/{{address}}";
    public boolean printMalformedEntries = false;
    public boolean ignoreInternalLogs = true;
    public int[] timeTakenBuckets = new int[] { 100, 500, 1000, 5000, 10000, 50000 };
    public int truncateWideColumns = 100;
    public int ipRequestCountThreshold = 5;
    public int userAgentRequestCountThreshold = 3;
    public int fileRequestCountThreshold = 0;
    public int queryRequestCountThreshold = 0;
    public int refererRequestCountThreshold = 0;
    public int logVerbosity = 1;

    public static void load() throws IOException {
        if(!CONFIG_DIR.exists()) {
            try {
                if(!CONFIG_DIR.mkdirs()) {
                    Logger.log("Failed to create config directory at %s", Logger.ERROR,
                            CONFIG_DIR.getAbsolutePath());
                    System.exit(1);
                }
            } catch(Exception e) {
                Logger.log("Failed to create config directory at %s", Logger.ERROR,
                        CONFIG_DIR.getAbsolutePath());
                Logger.log(e, Logger.ERROR);
                System.exit(1);
            }
        }
        if(!FILE.exists()) {
            Logger.log("Creating missing config.json at %s", Logger.WARN, FILE.getAbsolutePath());
            Config.instance.write();
            System.exit(1);
        } else {
            String json = Files.readString(FILE.toPath(), StandardCharsets.UTF_8);
            Config.instance = Utils.GSON.fromJson(json, Config.class);
        }
        if(instance.logVerbosity < 0 || instance.logVerbosity > 4) {
            System.err.println("[ERROR] logVerbosity must be between 0 and 4 (inclusive)");
            System.exit(1);
        }
        if(instance.timeTakenBuckets.length == 0) {
            Logger.log("timeTakenBuckets must have at least one entry", Logger.ERROR);
            System.exit(1);
        }
        inputDF = new SimpleDateFormat(instance.inputDateFormat);
        outputDF = new SimpleDateFormat(instance.outputDateFormat);
        if(instance.truncateWideColumns == 0)
            instance.truncateWideColumns = Integer.MAX_VALUE;
    }

    public void write() throws IOException {
        Files.writeString(FILE.toPath(), PRETTY_GSON.toJson(this));
    }

    public String toString() {
        return PRETTY_GSON.toJson(this);
    }

    public static File getAccessLogDirectory() {
        return new File(Utils.replaceTildeInPath(instance.accessLogDirectory));
    }

    public static File getOutputDir() {
        return new File(Utils.replaceTildeInPath(instance.outputDirectory));
    }

    public static File getOutputFile() {
        return new File(getOutputDir(), instance.outputFileName);
    }

    public static File getMonthlySubpageFile(int year, int month) {
        return new File(getOutputDir(),
                instance.monthSubpagePattern.replace("{{year}}", String.valueOf(year))
                        .replace("{{month}}", String.format("%02d", month)));
    }

    public static DateFormat getInputDateFormat() {
        return inputDF;
    }

    public static DateFormat getOutputDateFormat() {
        return outputDF;
    }
}
