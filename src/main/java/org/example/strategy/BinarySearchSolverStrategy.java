package org.example.strategy;


import org.example.domain.Duck;
import org.example.domain.Lane;
import org.example.domain.RaceAssignment;

public class BinarySearchSolverStrategy implements SolvingStrategy {

    private void sort(Duck[] ducks) {

        for (int i = 1; i < ducks.length; i++) {
            Duck aux = ducks[i];
            int j = i - 1;
            while (j >= 0 &&
                    (ducks[j].getEndurance() > aux.getEndurance() ||
                            (ducks[j].getEndurance() == aux.getEndurance() &&
                                    ducks[j].getSpeed() < aux.getSpeed()))) {
                ducks[j + 1] = ducks[j];
                j--;
            }
            ducks[j + 1] = aux;
        }
    }

    private boolean fits(double T, int distance, double speed) {
        return (2.0 * (double) distance) / (double) speed <= T + 1e-12;
    }

    private boolean feasible(double T, Duck[] ducks, Lane[] lanes, Duck[] chosenOut) {
        int n = ducks.length;
        int m = lanes.length;

        Duck[] order = new Duck[n];
        for (int i = 0; i < n; i++) order[i] = ducks[i];
        sort(order);

        java.util.Set<Long> usedIDs = new java.util.HashSet<>();
        int p = 0;
        for (int j = 0; j < m; j++) {
            int d = lanes[j].getDistanta();
            while (p < n) {
                Duck newDuck = order[p];
                long duckID = newDuck.getID();

                if (!usedIDs.contains(duckID) && fits(T, d, newDuck.getSpeed())) {
                    usedIDs.add(duckID);
                    chosenOut[j] = newDuck;
                    p++;
                    break;
                }
                p++;
            }
            if (chosenOut[j] == null) return false;
        }
        return true;
    }


    @Override
    public RaceAssignment solve(Duck[] ducks, Lane[] lanes) {
        if (ducks == null || lanes == null || lanes.length == 0) return null;

        int maxDistance = 0;
        double minViteza = Integer.MAX_VALUE;
        for (int i = 0; i < lanes.length; i++) if (lanes[i].getDistanta() > maxDistance) maxDistance = lanes[i].getDistanta();
        for (int i = 0; i < ducks.length; i++) if (ducks[i].getSpeed() < minViteza) minViteza = ducks[i].getSpeed();

        double lowestTime = 0.0;
        double highestTime = (2.0 * (double) maxDistance) / (double) minViteza;
        double bestTime = highestTime;
        Duck[] bestAssign = null;


        Duck[] auxDucks = new Duck[lanes.length];
        if (!feasible(highestTime, ducks, lanes, auxDucks)) {
            return null;
        }
        bestAssign = auxDucks;

        final double EPS = 1e-4;
        while (highestTime - lowestTime > EPS) {
            double mid = (lowestTime + highestTime) / 2.0;
            auxDucks = new Duck[lanes.length];
            if (feasible(mid, ducks, lanes, auxDucks)) {
                bestTime = mid;
                bestAssign = auxDucks;
                highestTime = mid;
            } else {
                lowestTime = mid;
            }
        }

        return new RaceAssignment(bestTime, bestAssign, lanes);
    }
}
