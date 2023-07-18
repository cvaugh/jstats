package dev.cvaugh.jstats;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class LogEntry implements Comparable<LogEntry> {
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
            if(v == null)
                return;
            switch(e) {
            case CLIENT_IP -> clientIP = v;
            case UNDERLYING_PEER_IP -> underlyingPeerIP = v;
            case LOCAL_IP -> localIP = v;
            case SIZE_OF_RESPONSE -> sizeOfResponse = Long.parseLong(v);
            case SIZE_OF_RESPONSE_CLF -> sizeOfResponse = v.equals("-") ? 0L : Long.parseLong(v);
            case TIME_TO_SERVE_US -> timeToServeUs = Long.parseLong(v);
            case FILENAME -> filename = v;
            case REMOTE_HOSTNAME -> remoteHostname = v;
            case REMOTE_HOSTNAME_UNDERLYING -> remoteHostnameUnderlying = v;
            case REQUEST_PROTOCOL -> requestProtocol = v;
            case NUM_KEEPALIVE -> numKeepalive = Integer.parseInt(v);
            case REMOTE_LOGNAME -> remoteLogname = v;
            case REQUEST_ERROR_LOG_ID -> requestErrorLogID = v;
            case REQUEST_METHOD -> requestMethod = v;
            case PORT -> port = Integer.parseInt(v);
            case PID -> pid = Integer.parseInt(v);
            case QUERY -> query = v;
            case FIRST_LINE -> firstLine = v;
            case HANDLER -> handler = v;
            case STATUS -> status = Integer.parseInt(v);
            case STATUS_FINAL -> statusFinal = Integer.parseInt(v);
            case TIME -> {
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
            case TIME_TO_SERVE_S -> timeToServeS = Integer.parseInt(v);
            case REMOTE_USER -> remoteUser = v;
            case URL -> url = v;
            case SERVER_NAME -> serverName = v;
            case SERVER_NAME_UCN -> serverNameUCN = v;
            case CONNECTION_STATUS -> {
                switch(v.charAt(0)) {
                case 'X' -> connectionStatus = ConnectionStatus.ABORTED;
                case '+' -> connectionStatus = ConnectionStatus.KEPT_ALIVE;
                case '-' -> connectionStatus = ConnectionStatus.CLOSED;
                default -> connectionStatus = ConnectionStatus.UNSET;
                }
            }
            case BYTES_RECEIVED -> bytesReceived = Long.parseLong(v);
            case BYTES_SENT -> bytesSent = Long.parseLong(v);
            case BYTES_TRANSFERRED -> bytesTransferred = Long.parseLong(v);
            case REFERER -> referer = v;
            case USER_AGENT -> userAgent = v;
            }
        }
    }

    @Override
    public int compareTo(LogEntry e) {
        return Long.compare(time, e.time);
    }
}
