package org.example.domain;

public class RaceAssignment {

    private double timpTotal;
    private Duck[] assignedDucks;
    private Lane[] lanes;
    private double[] laneTimes;

    public RaceAssignment(double timpTotal, Duck[] assignedDucks, Lane[] lanes) {
        this.timpTotal = timpTotal;
        this.assignedDucks = assignedDucks;
        this.lanes = lanes;
        this.laneTimes = new double[assignedDucks.length];
        for (int i = 0; i < assignedDucks.length; i++) {
            int d = lanes[i].getDistanta();
            double v = assignedDucks[i].getSpeed();
            laneTimes[i] = (2.0 * (double) d) / (double) v;
        }
    }

    public double getTimpTotal() {
        return timpTotal;
    }
    public Duck[] getAssignedDucks() {
        return assignedDucks;
    }
    public Lane[] getLanes() {
        return lanes;
    }
    public double[] getLaneTimes() {
        return laneTimes;
    }
}
