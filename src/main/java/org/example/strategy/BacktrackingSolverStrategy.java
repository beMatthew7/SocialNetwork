package org.example.strategy;


import org.example.domain.Duck;
import org.example.domain.Lane;
import org.example.domain.RaceAssignment;

public class BacktrackingSolverStrategy implements SolvingStrategy {

    private double bestTime;
    private Duck[] bestAssign;

    private double timeFor(Lane lane, Duck duck) {
        return (2.0 * lane.getDistanta()) / duck.getSpeed();
    }

    private void backtrack(int laneIdx, Duck[] ducks, Lane[] lanes, boolean[] used, Duck[] current, double currentMax) {
        if (laneIdx == lanes.length) {
            if (currentMax < bestTime) {
                bestTime = currentMax;
                for (int i = 0; i < current.length; i++) {
                    bestAssign[i] = current[i];
                }
            }
            return;
        }

        for (int i = 0; i < ducks.length; i++) {
            if (used[i]) continue;

            double time = timeFor(lanes[laneIdx], ducks[i]);

            used[i] = true;
            current[laneIdx] = ducks[i];

            backtrack(laneIdx + 1, ducks, lanes, used, current, Math.max(currentMax, time));

            used[i] = false;
        }
    }

    @Override
    public RaceAssignment solve(Duck[] ducks, Lane[] lanes) {
        if (ducks == null || lanes == null || lanes.length == 0) return null;

        boolean[] used = new boolean[ducks.length];
        Duck[] current = new Duck[lanes.length];
        bestAssign = new Duck[lanes.length];
        bestTime = Double.POSITIVE_INFINITY;

        backtrack(0, ducks, lanes, used, current, 0.0);

        if (bestTime == Double.POSITIVE_INFINITY) return null;
        return new RaceAssignment(bestTime, bestAssign, lanes);
    }
}
