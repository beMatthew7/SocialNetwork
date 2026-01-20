package org.example.domain;

import org.example.interactions.Card;

public class SwimmingDuck extends Duck implements Swimmer {
    public SwimmingDuck(String username, String email, String password, DuckType type, double speed, double endurance) {
        super(username, email, password, type, speed, endurance);
    }

    @Override
    public void swim() {
        System.out.println(getUsername() + " inoata!");
    }
}