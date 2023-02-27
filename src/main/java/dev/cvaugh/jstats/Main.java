package dev.cvaugh.jstats;

import java.io.IOException;

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
        System.out.println(Config.instance);
    }
}
