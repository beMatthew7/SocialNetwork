package org.example.utils.observer;

import org.example.interactions.RaceEvent;

/**
 * Wrapper pentru notificări de cursă care include și mesajul
 */
public class RaceNotification {
    private final RaceEvent event;
    private final String message;

    public RaceNotification(RaceEvent event, String message) {
        this.event = event;
        this.message = message;
    }

    public RaceEvent getEvent() {
        return event;
    }

    public String getMessage() {
        return message;
    }
}

