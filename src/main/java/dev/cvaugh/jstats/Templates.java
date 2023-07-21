package dev.cvaugh.jstats;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public final class Templates {
    public static final String TRUNCATED_CELL =
            "<span class=\"truncated\" title=\"%s\">%s<span class=\"truncation-marker\">&raquo;</span></span>";
    public static final String TIME_ROW =
            "<tr><td>%s</td><td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String MONTHLY_SUBPAGES_ROW =
            "<tr><td>%d</td><td><a href=\"%s\">%s</a></td></tr>\n";
    public static final String MONTHLY_SUBPAGES_ROW_MISSING =
            "<tr><td>%d</td><td>%s</td></tr>\n";
    public static final String PORTS_ROW =
            "<tr><td class=\"left\">%d</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String USERS_ROW =
            "<tr><td class=\"left\">%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String IP_ROW =
            "<tr><td class=\"left\">%s</td><td><a href=\"%s\">View</a></td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String USER_AGENTS_ROW =
            "<tr><td class=\"left\">%s</td><td>%d</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String FILES_ROW =
            "<tr><td class=\"left\">%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String QUERIES_ROW =
            "<tr><td class=\"left\">%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String RESPONSES_ROW =
            "<tr><td>%d</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String TIME_TAKEN_ROW = "<tr><td>%s%s%s</td><td>%d</td><td>%s</td></tr>\n";

    public static String read(String name) throws IOException {
        File file = new File(Config.TEMPLATE_DIR, name);
        if(Config.TEMPLATE_DIR.exists() && Config.TEMPLATE_DIR.isDirectory() && file.exists())
            return Files.readString(file.toPath());
        InputStream in = Utils.class.getResourceAsStream("/templates/" + name);
        if(in == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String template = String.join("\n", reader.lines().toList());
        reader.close();
        in.close();
        return template;
    }

    public static String read(OutputSection section) throws IOException {
        return read(section.name().toLowerCase() + ".html");
    }
}
