package common.network;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // Message types
    public static final String TYPE_REGISTRATION = "REGISTRATION";
    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_INVENTORY_UPDATE = "INVENTORY_UPDATE";
    public static final String TYPE_REPORT_REQUEST = "REPORT_REQUEST";
    public static final String TYPE_REPORT_RESPONSE = "REPORT_RESPONSE";

    private String type;
    private Object payload;
    private String sourceBranch;
    private String targetBranch;
    private long timestamp;

    public NetworkMessage(String type, Object payload, String sourceBranch, String targetBranch) {
        this.type = type;
        this.payload = payload;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public Object getPayload() { return payload; }

    public void setPayload(Object payload) { this.payload = payload; }

    public String getSourceBranch() { return sourceBranch; }

    public void setSourceBranch(String sourceBranch) { this.sourceBranch = sourceBranch; }

    public String getTargetBranch() { return targetBranch; }

    public void setTargetBranch(String targetBranch) { this.targetBranch = targetBranch; }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "type='" + type + '\'' +
                ", sourceBranch='" + sourceBranch + '\'' +
                ", targetBranch='" + targetBranch + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}