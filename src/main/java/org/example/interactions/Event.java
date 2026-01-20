package org.example.interactions;

import org.example.domain.Entity;
import org.example.domain.User;

import java.util.ArrayList;
import java.util.List;

public abstract class Event extends Entity<Long> {
    private final String name;
    private final User organizer;
    private final List<User> subscribers = new ArrayList<>();

    protected Event(String name, User organizer){
        this.name = name;
        this.organizer = organizer;
    }

    public String getName() {
        return name;
    }

    public List<User> getSubscribers() {
        return subscribers;
    }

    public void subscribe(User u){
        if(u != null && !subscribers.contains(u)){
            subscribers.add(u);
            //u.getEventHistory().add("Te ai abonat la" + name);
        }
    }

    public User getOrganizer() {
        return organizer;
    }

    public void unsubscribe(User u){
        if(u != null && subscribers.contains(u)){
            subscribers.remove(u);
            //u.getEventHistory().add("Te ai dezabonat de la" + name);
        }
    }



    protected void notifySubscribers(String message){
        for (User u: subscribers){
            u.getEventHistory().add("Evenimentul" + name + " " + message);
        }
    }
}
