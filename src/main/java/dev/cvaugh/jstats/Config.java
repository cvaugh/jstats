package dev.cvaugh.jstats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Config {
    public static final File FILE = new File("config.json");
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Config instance = new Config();

    public String accessLogDirectory = "/var/log/apache2";
    public String accessLogName = "access.log";
    public boolean readRotatedLogs = true;
    public String logFormat =
            "%v:%p %h %l %u %t \"%r\" %s:%>s %I %O \"%{Referer}i\" \"%{User-Agent}i\" %D %k %f \"%U\" \"%q\"";
    public String outputFilePath = "~/simplestats.html";
    public String inputDateFormat = "%d/%b/%Y:%H:%M:%S %z";
    public String outputDateFormat = "%e %b %Y %I:%M:%S %p";
    public String whoisTool = "https://iplocation.io/ip/{{address}}";
    public boolean ignoreInternalLogs = true;
    public boolean notifyOnMalformed = false;

    public static void load() throws IOException {
        if(!FILE.exists()) {
            Config.instance.write();
        } else {
            String json = Files.readString(FILE.toPath(), StandardCharsets.UTF_8);
            Config.instance = Utils.GSON.fromJson(json, Config.class);
        }
    }

    public void write() throws IOException {
        Files.writeString(FILE.toPath(), PRETTY_GSON.toJson(this));
    }

    public String toString() {
        return PRETTY_GSON.toJson(this);
    }

    public File getAccessLogDirectory() {
        return new File(Utils.replaceTildeInPath(accessLogDirectory));
    }

    public File getOutputFile() {
        return new File(Utils.replaceTildeInPath(outputFilePath));
    }
}
