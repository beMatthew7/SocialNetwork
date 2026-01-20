package org.example.interactions;

import org.example.domain.Duck;
import org.example.domain.DuckType;
import org.example.domain.Entity;

import java.util.ArrayList;
import java.util.List;


public abstract class Card<T extends Duck> extends Entity<Long> {
    private String cardName;
    private DuckType targetType;
    private List<T> members;


    public Card(String cardName, DuckType targetType){
        this.cardName = cardName;
        this.targetType = targetType;
        this.members = new ArrayList<>();
    }


    public boolean canJoin(Duck duck) {
        if (targetType == DuckType.FLYING) {
            return duck.getType() == DuckType.FLYING ||
                    duck.getType() == DuckType.FLYING_AND_SWIMMING;
        } else if (targetType == DuckType.SWIMMING) {
            return duck.getType() == DuckType.SWIMMING ||
                    duck.getType() == DuckType.FLYING_AND_SWIMMING;
        } else if (targetType == DuckType.FLYING_AND_SWIMMING) {
            return duck.getType() == DuckType.FLYING_AND_SWIMMING;
        }
        return false;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public List<T> getMembers() {
        return members;
    }

    public void addMember(T duck) {
        if (!members.contains(duck)) {
            members.add(duck);
        }
    }

    public void removeMember(T duck) {
        members.remove(duck);
    }

    public double getAveragePerformance() {
        if (members.isEmpty()) {
            return 0.0;
        }
        double sumaViteze = 0.0;
        double sumaRezistente = 0.0;
        for (T duck : members) {
            sumaViteze += duck.getSpeed();
            sumaRezistente += duck.getEndurance();
        }
        double mediaViteze = sumaViteze / members.size();
        double mediaRezistente = sumaRezistente / members.size();
        return (mediaViteze + mediaRezistente) / 2.0;
    }

    public double getAverageSpeed() {
        if (members.isEmpty()) {
            return 0.0;
        }
        return members.stream()
                .mapToDouble(Duck::getSpeed)
                .average()
                .orElse(0.0);
    }

    public double getAverageEndurance() {
        if (members.isEmpty()) {
            return 0.0;
        }
        return members.stream()
                .mapToDouble(Duck::getEndurance)
                .average()
                .orElse(0.0);
    }

    public DuckType getTargetType() {
        return targetType;
    }
}