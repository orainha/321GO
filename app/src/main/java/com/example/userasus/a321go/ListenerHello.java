package com.example.userasus.a321go;

import java.util.ArrayList;

public class ListenerHello {

    private ArrayList<HelloListener> listeners = new ArrayList<HelloListener>();

    public void addListener(HelloListener toAdd) {
        listeners.add(toAdd);
    }

    public void notifyListener() {
        // Notify everybody that may be interested.
        for (HelloListener hl : listeners)
            hl.answer();
    }
}
