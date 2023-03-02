package dev.cvaugh.jstats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum OutputSection {
    GENERATED_DATE, HEADER(LogElement.TIME),
    OVERALL(LogElement.REMOTE_HOSTNAME, LogElement.BYTES_SENT),
    YEARLY_TABLE(LogElement.TIME, LogElement.BYTES_SENT, LogElement.REMOTE_HOSTNAME),
    MONTHLY_TABLE(LogElement.TIME, LogElement.BYTES_SENT, LogElement.REMOTE_HOSTNAME),
    DAY_OF_WEEK_TABLE(LogElement.TIME, LogElement.BYTES_SENT, LogElement.REMOTE_HOSTNAME),
    HOURLY_TABLE(LogElement.TIME, LogElement.BYTES_SENT, LogElement.REMOTE_HOSTNAME),
    IP_TABLE(LogElement.TIME, LogElement.BYTES_SENT, LogElement.REMOTE_HOSTNAME),
    USERS_TABLE(LogElement.TIME, LogElement.BYTES_SENT, LogElement.REMOTE_USER),
    USER_AGENT_TABLE(LogElement.TIME, LogElement.BYTES_SENT, LogElement.USER_AGENT),
    FILES_TABLE(LogElement.TIME, LogElement.FILENAME, LogElement.BYTES_SENT),
    QUERIES_TABLE(LogElement.QUERY), REFERERS_TABLE(LogElement.REFERER),
    RESPONSES_TABLE(LogElement.STATUS_FINAL, LogElement.BYTES_SENT),
    TIME_TAKEN_TABLE(LogElement.TIME_TO_SERVE_US), FOOTER;
    // TODO protocols, servers, PID, request method, local IP

    private final List<LogElement> requiredElements = new ArrayList<>();

    OutputSection() {}

    OutputSection(LogElement... required) {
        requiredElements.addAll(List.of(required));
    }

    public boolean isUsable(Set<LogElement> elements) {
        if(requiredElements.isEmpty())
            return true;
        return elements.containsAll(requiredElements);
    }
}
