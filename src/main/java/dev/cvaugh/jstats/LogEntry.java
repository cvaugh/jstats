package dev.cvaugh.jstats;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class LogEntry {
    public Map<LogElement, String> elements;

    public String clientIP = null;
    public String underlyingPeerIP = null;
    public String localIP = null;
    public long sizeOfResponse = -1;
    public long timeToServeUs = -1;
    public String filename = null;
    public String remoteHostname = null;
    public String remoteHostnameUnderlying = null;
    public String requestProtocol = null;
    public int numKeepalive = -1;
    public String remoteLogname = null;
    public String requestErrorLogID = null;
    public String requestMethod = null;
    public int port = -1;
    public int pid = -1;
    public String query = null;
    public String firstLine = null;
    public String handler = null;
    public int status = -1;
    public int statusFinal = -1;
    public long time = -1;
    public int timeToServeS = -1;
    public String remoteUser = null;
    public String url = null;
    public String serverName = null;
    public String serverNameUCN = null;
    public ConnectionStatus connectionStatus = ConnectionStatus.UNSET;
    public long bytesReceived = -1;
    public long bytesSent = -1;
    public long bytesTransferred = -1;
    public String referer = null;
    public String userAgent = null;

    public LogEntry(Map<LogElement, String> elements) {
        this.elements = elements;
    }

    public void process() {
        for(LogElement e : LogElement.values()) {
            String v = elements.get(e);
            switch(e) {
            case CLIENT_IP -> {
                if(v != null) {
                    clientIP = v;
                }
            }
            case UNDERLYING_PEER_IP -> {
                if(v != null) {
                    underlyingPeerIP = v;
                }
            }
            case LOCAL_IP -> {
                if(v != null) {
                    localIP = v;
                }
            }
            case SIZE_OF_RESPONSE -> {
                if(v != null) {
                    sizeOfResponse = Long.parseLong(v);
                }
            }
            case SIZE_OF_RESPONSE_CLF -> {
                if(v != null) {
                    sizeOfResponse = v.equals("-") ? 0L : Long.parseLong(v);
                }
            }
            case TIME_TO_SERVE_US -> {
                if(v != null) {
                    timeToServeUs = Long.parseLong(v);
                }
            }
            case FILENAME -> {
                if(v != null) {
                    filename = v;
                }
            }
            case REMOTE_HOSTNAME -> {
                if(v != null) {
                    remoteHostname = v;
                }
            }
            case REMOTE_HOSTNAME_UNDERLYING -> {
                if(v != null) {
                    remoteHostnameUnderlying = v;
                }
            }
            case REQUEST_PROTOCOL -> {
                if(v != null) {
                    requestProtocol = v;
                }
            }
            case NUM_KEEPALIVE -> {
                if(v != null) {
                    numKeepalive = Integer.parseInt(v);
                }
            }
            case REMOTE_LOGNAME -> {
                if(v != null) {
                    remoteLogname = v;
                }
            }
            case REQUEST_ERROR_LOG_ID -> {
                if(v != null) {
                    requestErrorLogID = v;
                }
            }
            case REQUEST_METHOD -> {
                if(v != null) {
                    requestMethod = v;
                }
            }
            case PORT -> {
                if(v != null) {
                    port = Integer.parseInt(v);
                }
            }
            case PID -> {
                if(v != null) {
                    pid = Integer.parseInt(v);
                }
            }
            case QUERY -> {
                if(v != null) {
                    query = v;
                }
            }
            case FIRST_LINE -> {
                if(v != null) {
                    firstLine = v;
                }
            }
            case HANDLER -> {
                if(v != null) {
                    handler = v;
                }
            }
            case STATUS -> {
                if(v != null) {
                    status = Integer.parseInt(v);
                }
            }
            case STATUS_FINAL -> {
                if(v != null) {
                    statusFinal = Integer.parseInt(v);
                }
            }
            case TIME -> {
                if(v != null) {
                    Date date;
                    String stripped = v.substring(1, v.length() - 1);
                    try {
                        date = Config.getInputDateFormat().parse(stripped);
                    } catch(ParseException ex) {
                        Logger.log("Failed to parse date: %s", Logger.WARN, stripped);
                        continue;
                    }
                    time = date.getTime();
                }
            }
            case TIME_TO_SERVE_S -> {
                if(v != null) {
                    timeToServeS = Integer.parseInt(v);
                }
            }
            case REMOTE_USER -> {
                if(v != null) {
                    remoteUser = v;
                }
            }
            case URL -> {
                if(v != null) {
                    url = v;
                }
            }
            case SERVER_NAME -> {
                if(v != null) {
                    serverName = v;
                }
            }
            case SERVER_NAME_UCN -> {
                if(v != null) {
                    serverNameUCN = v;
                }
            }
            case CONNECTION_STATUS -> {
                if(v != null) {
                    switch(v.charAt(0)) {
                    case 'X' -> connectionStatus = ConnectionStatus.ABORTED;
                    case '+' -> connectionStatus = ConnectionStatus.KEPT_ALIVE;
                    case '-' -> connectionStatus = ConnectionStatus.CLOSED;
                    default -> connectionStatus = ConnectionStatus.UNSET;
                    }
                }
            }
            case BYTES_RECEIVED -> {
                if(v != null) {
                    bytesReceived = Long.parseLong(v);
                }
            }
            case BYTES_SENT -> {
                if(v != null) {
                    bytesSent = Long.parseLong(v);
                }
            }
            case BYTES_TRANSFERRED -> {
                if(v != null) {
                    bytesTransferred = Long.parseLong(v);
                }
            }
            case REFERER -> {
                if(v != null) {
                    referer = v;
                }
            }
            case USER_AGENT -> {
                if(v != null) {
                    userAgent = v;
                }
            }
            }
        }
    }

    public String toString() {
        return String.format("<LogEntry with %d element%s>", elements.size(),
                elements.size() == 1 ? "" : "s");
    }
}
