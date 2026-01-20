package org.example.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class User extends Entity<Long> {
    private String username;
    private String email;
    private String password;

    protected List<User> friends;
    protected List<String> eventHistory;

    public User(String username, String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;

        this.friends = new ArrayList<>();
        this.eventHistory = new ArrayList<>();
    }

    public String getUsername(){
        return this.username;
    }

    public void login(){
        System.out.println("S-a logat userul: " + this.username + " s-a autentificat.");
        this.eventHistory.add("Autentificare la data: " + java.time.LocalDateTime.now());
    }

    public void logout(){
        System.out.println("S-a delogat userul: " + this.username);
        this.eventHistory.add("Delogare la data: " + java.time.LocalDateTime.now());
    }


    public void addFriend(User friend) {
        if (friend == null || friend.equals(this)) {
            System.err.println("Eroare: Nu se poate adăuga un prieten invalid sau pe sine însuși.");
            return;
        }

        if (!this.friends.contains(friend)) {
            this.friends.add(friend);
            this.getEventHistory().add("Adăugat prieten: " + friend.getUsername());
            friend.addFriend(this);
        }
    }

    public void deleteFriend(User friend) {
        if (friend == null || friend.equals(this)) {
            System.err.println("Eroare: Nu se poate șterge un prieten invalid sau pe sine însuși.");
            return;
        }
        if (this.friends.contains(friend)) {
            this.friends.remove(friend);
            this.getEventHistory().add("Șters prieten: " + friend.getUsername());
            friend.deleteFriend(this);
        }
    }


    public String getEmail() {
        return email;
    }

    public List<String> getEventHistory() {
        return eventHistory;
    }

    public void setEventHistory(List<String> eventHistory) {
        this.eventHistory = eventHistory;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getID(), user.getID());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getID());
    }

    public List<User> getFriends() {
        return friends;
    }
}
