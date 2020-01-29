package com.example.userasus.a321go;

import java.util.ArrayList;

public class ListenerConnection {

    private ArrayList<ConnectionListener> listeners = new ArrayList<ConnectionListener>();

    public void addListener(ConnectionListener toAdd) {
        listeners.add(toAdd);
    }

    public void notifyListener() {
        // Notify everybody that may be interested.
        for (ConnectionListener hl : listeners)
            hl.answer();
    }
}
