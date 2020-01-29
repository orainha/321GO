package com.example.userasus.a321go;

import java.util.ArrayList;

public class ListenerLatency {

    private ArrayList<LatencyListener> listeners = new ArrayList<LatencyListener>();

    public void addListener(LatencyListener toAdd) {
        listeners.add(toAdd);
    }

    public void notifyListener() {
        // Notify everybody that may be interested.
        for (LatencyListener hl : listeners)
            hl.answer();
    }
}
