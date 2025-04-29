package common.network;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_INVENTORY_UPDATE = "INVENTORY_UPDATE";
    public static final String TYPE_STOCK_ALERT = "STOCK_ALERT";
    public static final String TYPE_REPORT_REQUEST = "REPORT_REQUEST";
    public static final String TYPE_REPORT_RESPONSE = "REPORT_RESPONSE";
    
    private String type;
    private Object payload;
    private String sourceBranch;
    private String destinationBranch;
    
    public NetworkMessage(String type, Object payload, String sourceBranch, String destinationBranch) {
        this.type = type;
        this.payload = payload;
        this.sourceBranch = sourceBranch;
        this.destinationBranch = destinationBranch;
    }
    
    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
    
    public String getSourceBranch() { return sourceBranch; }
    public void setSourceBranch(String sourceBranch) { this.sourceBranch = sourceBranch; }
    
    public String getDestinationBranch() { return destinationBranch; }
    public void setDestinationBranch(String destinationBranch) { this.destinationBranch = destinationBranch; }
}