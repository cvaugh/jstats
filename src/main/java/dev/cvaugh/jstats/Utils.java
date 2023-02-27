package dev.cvaugh.jstats;

import com.google.gson.Gson;

public class Utils {
    public static final Gson GSON = new Gson();

    public static String replaceTildeInPath(String path) {
        return path.replaceFirst("^~", System.getProperty("user.home").replaceAll("\\\\", "/"));
    }
}
