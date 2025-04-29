package common.models;

import java.io.Serializable;

public class Branch implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String HEADQUARTERS = "Nairobi";
    public static final String BRANCH_NAKURU = "Nakuru";
    public static final String BRANCH_MOMBASA = "Mombasa";
    public static final String BRANCH_KISUMU = "Kisumu";
    
    private int id;
    private String name;
    private String location;
    private String ipAddress;
    private int port;
    
    public Branch(int id, String name, String location, String ipAddress, int port) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.ipAddress = ipAddress;
        this.port = port;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    @Override
    public String toString() {
        return name + " (" + location + ")";
    }
}