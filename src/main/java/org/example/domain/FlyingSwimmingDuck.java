package org.example.domain;

import org.example.interactions.Card;

public class FlyingSwimmingDuck extends Duck implements Flyer, Swimmer {
    public FlyingSwimmingDuck(String username, String email, String password, DuckType type, double speed, double endurance) {
        super(username, email, password, type, speed, endurance);
    }

    @Override
    public void fly() {
        System.out.println(getUsername() + " zboara!");
    }

    @Override
    public void swim() {
        System.out.println(getUsername() + " inoata!");
    }
}