package dev.cvaugh.jstats;

public enum LogElement {
    CLIENT_IP("%a", "\\\\S+"), UNDERLYING_PEER_IP("%{c}a", "\\\\S+"), LOCAL_IP("%A", "\\\\S+"),
    SIZE_OF_RESPONSE("%B", "[0-9]+"), SIZE_OF_RESPONSE_CLF("%b", "[0-9]+|-"),
    TIME_TO_SERVE_US("%D", "[0-9]+"), FILENAME("%f", "\\\\S+"), REMOTE_HOSTNAME("%h", "\\\\S+"),
    REMOTE_HOSTNAME_UNDERLYING("%{c}h", "\\\\S+"), REQUEST_PROTOCOL("%H", "\\\\S+"),
    NUM_KEEPALIVE("%k", "[0-9]+"), REMOTE_LOGNAME("%l", "\\\\S+"),
    REQUEST_ERROR_LOG_ID("%L", "[0-9]+|-"), REQUEST_METHOD("%m", "\\\\S+"), PORT("%p", "[0-9]+"),
    PID("%P", "[0-9]+"), QUERY("%q", "\\\\S*"), FIRST_LINE("%r", ".*"), HANDLER("%R", "\\\\S+"),
    STATUS("%s", "[0-9]+"), STATUS_FINAL("%>s", "[0-9]+"), TIME("%t", "\\\\[.*\\\\]"),
    TIME_TO_SERVE_S("%T", "[0-9]+"), REMOTE_USER("%u", "\\\\S+"), URL("%U", ".*"),
    SERVER_NAME("%v", "\\\\S+"), SERVER_NAME_UCN("%V", "\\\\S+"),
    CONNECTION_STATUS("%X", "[\\\\+|\\\\-|X]"), BYTES_RECEIVED("%I", "[0-9]+"),
    BYTES_SENT("%O", "[0-9]+"), BYTES_TRANSFERRED("%S", "[0-9]+"), REFERER("%{Referer}i", ".*"),
    USER_AGENT("%{User-Agent}i", ".*");

    final String format;
    final String groupName;
    final String regex;

    LogElement(String format, String regex) {
        this.format = format;
        this.groupName = name().toLowerCase().replaceAll("_", "");
        this.regex = regex;
    }

    public String toString() {
        return format;
    }
}
