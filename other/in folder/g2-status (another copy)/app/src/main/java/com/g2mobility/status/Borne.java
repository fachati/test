package com.g2mobility.status;

import java.io.Serializable;


public class Borne implements Serializable {

    private static final long serialVersionUID = 8039423065260368820L;
    private String address;
    private String nodeID;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Borne)) return false;

        Borne borne = (Borne) o;

        if (!address.equals(borne.address)) return false;
        if (!nodeID.equals(borne.nodeID)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Borne{" +
                "address='" + address + '\'' +
                ", nodeID='" + nodeID + '\'' +
                '}';
    }

    public String getAddress() {
        return address;
    }

    public String getNodeID() {
        return this.nodeID;
    }
}