package dev.cvaugh.jstats;

public final class Templates {
    public static final String TRUNCATED_CELL =
            "<span class=\"truncated\" title=\"%s\">%s<span class=\"truncation-marker\">&raquo;</span></span>";
    public static final String TIME_ROW =
            "<tr><td>%s</td><td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
    public static final String MONTHLY_SUBPAGES_ROW =
            "<tr><td>%d</td><td><a href=\"%s\">%s</a></td></tr>\n";
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
}
