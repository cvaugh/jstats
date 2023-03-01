package dev.cvaugh.jstats;

public enum LogElement {
    CLIENT_IP("%a", "Client IP", "\\\\S+"),
    UNDERLYING_PEER_IP("%{c}a", "Underlying Peer IP", "\\\\S+"),
    LOCAL_IP("%A", "Local IP", "\\\\S+"), SIZE_OF_RESPONSE("%B", "Response Size", "[0-9]+"),
    SIZE_OF_RESPONSE_CLF("%b", "Response Size (CLF)", "[0-9]+|-"),
    TIME_TO_SERVE_US("%D", "Time Taken (Microseconds)", "[0-9]+"),
    FILENAME("%f", "Requested File", "\\\\S+"), REMOTE_HOSTNAME("%h", "Remote Hostname", "\\\\S+"),
    REMOTE_HOSTNAME_UNDERLYING("%{c}h", "Underlying Remote Hostname", "\\\\S+"),
    REQUEST_PROTOCOL("%H", "Request Protocol", "\\\\S+"),
    NUM_KEEPALIVE("%k", "Keepalive Requests", "[0-9]+"),
    REMOTE_LOGNAME("%l", "Remote Logname", "\\\\S+"),
    REQUEST_ERROR_LOG_ID("%L", "Error Log ID", "[0-9]+|-"),
    REQUEST_METHOD("%m", "Request Method", "\\\\S+"), PORT("%p", "Port", "[0-9]+"),
    PID("%P", "Child PID", "[0-9]+"), QUERY("%q", "Query", "\\\\S*"),
    FIRST_LINE("%r", "Request", ".*"), HANDLER("%R", "Handler", "\\\\S+"),
    STATUS("%s", "Status", "[0-9]+"), STATUS_FINAL("%>s", "Status (Final)", "[0-9]+"),
    TIME("%t", "Time", "\\\\[.*\\\\]"), TIME_TO_SERVE_S("%T", "Time Taken (Seconds)", "[0-9]+"),
    REMOTE_USER("%u", "User", "\\\\S+"), URL("%U", "URL", ".*"),
    SERVER_NAME("%v", "Server Name", "\\\\S+"),
    SERVER_NAME_UCN("%V", "Server Name (UCN)", "\\\\S+"),
    CONNECTION_STATUS("%X", "Connection Status", "[\\\\+|\\\\-|X]"),
    BYTES_RECEIVED("%I", "Bytes Received", "[0-9]+"), BYTES_SENT("%O", "Bytes Sent", "[0-9]+"),
    BYTES_TRANSFERRED("%S", "Bytes Transferred", "[0-9]+"), REFERER("%{Referer}i", "Referer", ".*"),
    USER_AGENT("%{User-Agent}i", "User Agent", ".*");

    final String format;
    final String description;
    final String groupName;
    final String regex;

    LogElement(String format, String description, String regex) {
        this.format = format;
        this.description = description;
        this.groupName = name().toLowerCase().replaceAll("_", "");
        this.regex = regex;
    }

    public String toString() {
        return format;
    }
}
