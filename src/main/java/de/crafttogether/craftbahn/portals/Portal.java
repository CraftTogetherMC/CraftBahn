package de.crafttogether.craftbahn.portals;

import de.crafttogether.craftbahn.util.CTLocation;

public class Portal {
    private Integer id;
    private String name;

    private String targetHost = null;
    private Integer targetPort = null;
    private CTLocation targetLocation = null;

    public Portal(String name, Integer id) {
        this.id = id;
        this.name = name;
    }
    public Portal(String name, Integer id, String targetHost, Integer targetPort, CTLocation targetLocation) {
        this.id = id;
        this.name = name;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.targetLocation = targetLocation;
    }

    public Integer getId() { return id; }
    public String getName() {
        return this.name;
    }

    public String getTargetHost() { return targetHost; }
    public Integer getTargetPort() { return targetPort; }
    public CTLocation getTargetLocation() { return targetLocation; }


    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    public void setTargetHost(String targetHost) { this.targetHost = targetHost; }
    public void setTargetPort(Integer targetPort) { this.targetPort = targetPort; }
    public void setTargetLocation(CTLocation targetLocation) { this.targetLocation = targetLocation; }

    public String toString() {
        return "id=" + id + ", name=" + name + ", targetHost=" + targetHost + ", targetPort=" + targetPort + ", location=[" + (targetLocation == null ? null : targetLocation.toString()) + "]";
    }
}