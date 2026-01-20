package org.example.domain;

import org.example.interactions.Card;

public class FlyingDuck extends Duck implements Flyer {
    public FlyingDuck(String username, String email, String password, DuckType type, double speed, double endurance) {
        super(username, email, password, type, speed, endurance);
    }

    @Override
    public void fly() {
        System.out.println(getUsername() + " zboara!");
    }
}