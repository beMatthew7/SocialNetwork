package org.example.strategy;

public class StrategyFactory {

    public SolvingStrategy createStrategy(StrategyType type){
        switch (type){
            case BINARY_SEARCH:
                return new BinarySearchSolverStrategy();

            case BACKTRACKING:
                return new BacktrackingSolverStrategy();
            default:
            throw new IllegalArgumentException("Tip de strategie necunoscut" + type);

        }

    }
}
