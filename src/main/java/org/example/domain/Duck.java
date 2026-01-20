package org.example.domain;

import org.example.interactions.Card;

public abstract class Duck extends User{
    private DuckType type;
    private double speed;
    private double endurance;

    private Card card;


    protected Duck(String username, String email, String password, DuckType type, double speed, double endurance) {
        super(username, email, password);
        this.type = type;
        this.speed = speed;
        this.endurance = endurance;

    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getEndurance() {
        return endurance;
    }

    public void setEndurance(double endurance) {
        this.endurance = endurance;
    }

    public DuckType getType() {
        return type;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    @Override
    public String toString() {
        return "Duck{" +
                "type=" + type +
                ", speed=" + speed +
                ", endurance=" + endurance +
                ", card=" + (card != null ? card.getCardName() : "null") +
                '}';
    }
}
