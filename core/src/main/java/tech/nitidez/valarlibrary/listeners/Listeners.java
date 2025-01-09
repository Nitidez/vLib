package tech.nitidez.valarlibrary.listeners;

import tech.nitidez.valarlibrary.listeners.player.PlayerProtocolListener;

public class Listeners {
    public static void setupListeners() {
        PlayerProtocolListener.registerPacketsListener();
    }
}
