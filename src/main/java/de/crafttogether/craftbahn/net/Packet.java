package de.crafttogether.craftbahn.net;

import java.io.Serializable;

public class Packet implements Serializable {
    String message;

    public Packet(String message) {
        this.message = message;
    }
}
