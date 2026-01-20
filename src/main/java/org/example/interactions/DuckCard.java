package org.example.interactions;

import org.example.domain.Duck;
import org.example.domain.DuckType;

public class DuckCard extends Card<Duck> {
    public DuckCard(String cardName, DuckType targetType){
        super(cardName, targetType);
    }
}
