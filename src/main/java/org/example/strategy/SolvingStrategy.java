package org.example.strategy;


import org.example.domain.Duck;
import org.example.domain.Lane;
import org.example.domain.RaceAssignment;

public interface SolvingStrategy {
    public RaceAssignment solve(Duck[] ducks, Lane[] lanes);
}
