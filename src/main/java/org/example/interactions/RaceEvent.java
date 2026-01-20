package org.example.interactions;

import org.example.domain.*;
import org.example.strategy.SolvingStrategy;
import org.example.strategy.StrategyFactory;
import org.example.strategy.StrategyType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RaceEvent extends Event {
    private final List<Duck> participants = new ArrayList<>();
    private final List<Lane> lanes = new ArrayList<>();
    public RaceEvent(String name, User organizer) {
        super(name, organizer);
    }

    public List<Duck> getParticipant() {
        return participants;
    }

    public void autoSelectParticipants(List<Duck> allDucks, List<Lane> lanes) {
        participants.clear();
        if (allDucks == null) {
            return;
        }
        this.lanes.addAll(lanes);

        int m = lanes.size();


        List<Duck> candidates = allDucks.stream()
                .filter(d -> d.getType() == DuckType.SWIMMING || d.getType() == DuckType.FLYING_AND_SWIMMING)
                .sorted((d1, d2) -> Double.compare(d2.getSpeed(), d1.getSpeed()))
                .collect(Collectors.toList());


        if (candidates.size() < m) {
            return;
        }


        participants.addAll(candidates.subList(0, m));
    }

    public String startRace(StrategyType type) {
        if (participants.isEmpty()) {
            return "NoParticipants";
        }
        if (lanes.isEmpty()) {
            return "No lanes";
        }
        ;

        SolvingStrategy strategy = new StrategyFactory().createStrategy(type);

        Duck[] duckArr = toStrategyDuckArray(participants);
        Lane[] laneArr = lanes.toArray(new Lane[0]);

        RaceAssignment assignment = strategy.solve(duckArr, laneArr);
        String summary = summarize(assignment);
        return summary;
    }

    private Duck[] toStrategyDuckArray(List<Duck> list) {
        return list.toArray(new Duck[0]);
    }

    private String summarize(RaceAssignment ra) {
        if (ra == null || ra.getAssignedDucks() == null || ra.getAssignedDucks().length == 0) {
            return "S a terminat cursa. Nu vem posibilitati";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("S a terminat cursa. Lanes=").append(ra.getLanes().length)
                .append(". Cel mai bun timp: ").append(String.format("%.4f", ra.getTimpTotal())).append("s\n");
        Duck[] asg = ra.getAssignedDucks();
        double[] times = ra.getLaneTimes();
        for (int i = 0; i < asg.length; i++) {
            Duck d = asg[i];
            sb.append("Lane ").append(i + 1).append(": ")
                    .append(d.getUsername() != null ? d.getUsername() : ("Duck" + d.getID()))
                    .append(" -> ").append(String.format("%.4f", times[i])).append("s\n");
        }
        return sb.toString();
    }

    public void addLane(Lane lane) {
        this.lanes.add(lane);
    }

    public void addParticipant(Duck duck) {
        this.participants.add(duck);
    }

    public List<Lane> getLanes() {
        return lanes;
    }
}


