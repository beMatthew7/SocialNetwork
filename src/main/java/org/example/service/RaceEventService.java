package org.example.service;
import org.example.domain.Duck;
import org.example.domain.Lane;
import org.example.domain.User;
import org.example.interactions.Event;
import org.example.interactions.RaceEvent;
import org.example.repository.EventHistoryRepository;
import org.example.repository.Repository;
import org.example.strategy.StrategyType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.example.utils.observer.Observable;
import org.example.utils.observer.Observer;
import org.example.utils.observer.RaceNotification;

public class RaceEventService implements Observable<RaceEvent> {
    private final Repository<Long, Event> eventRepo;
    private final EventHistoryRepository historyRepo;
    private final UserService userService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    

    private final List<Observer<RaceEvent>> observers = new ArrayList<>();


    private final List<Observer<RaceNotification>> notificationObservers = new ArrayList<>();

    public RaceEventService(Repository<Long, Event> eventRepo, EventHistoryRepository historyRepo, UserService userService) {
        this.eventRepo = eventRepo;
        this.historyRepo = historyRepo;
        this.userService = userService;
    }

    @Override
    public void addObserver(Observer<RaceEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<RaceEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(RaceEvent t) {
        observers.forEach(o -> o.update(t));
    }


    public void addNotificationObserver(Observer<RaceNotification> o) {
        notificationObservers.add(o);
    }

    public void removeNotificationObserver(Observer<RaceNotification> o) {
        notificationObservers.remove(o);
    }

    private void notifyWithMessage(RaceEvent event, String message) {
        RaceNotification notification = new RaceNotification(event, message);
        notificationObservers.forEach(o -> o.update(notification));
    }

    public List<RaceEvent> getAllRaceEvents() {
        return StreamSupport.stream(eventRepo.findAll().spliterator(), false)
                .filter(e -> e instanceof RaceEvent)
                .map(e -> (RaceEvent) e)
                .collect(Collectors.toList());
    }


    public List<RaceEvent> getEventsCreatedBy(User user) {
        return getAllRaceEvents().stream()
                .filter(e -> e.getOrganizer().getID().equals(user.getID()))
                .collect(Collectors.toList());
    }
    public void createRaceEvent(String name, User creator, List<Integer> laneLengths) {
        RaceEvent race = new RaceEvent(name, creator);


        List<Lane> lanes = new ArrayList<>();
        for (Integer length : laneLengths) {
            lanes.add(new Lane(length));
        }

        List<Duck> allDucks = new ArrayList<>();
        userService.getAllDucks().forEach(allDucks::add);

        race.autoSelectParticipants(allDucks, lanes);


        eventRepo.save(race);
        notifyObservers(race);
    }

    public void startRaceAsync(Long eventId, User executorUser) {
        Event event = eventRepo.findOne(eventId);

        RaceEvent race = (RaceEvent) event;
        if (race.getOrganizer() == null || !race.getOrganizer().getID().equals(executorUser.getID())) {
            throw new RuntimeException("Only the creator can start the race!");
        }
        // Run async
        CompletableFuture.runAsync(() -> {
            try {

                String startMsg = "Race '" + race.getName() + "' STARTED! Get ready...";
                notifyAllSubscribers(race, startMsg);
                notifyWithMessage(race, startMsg);

                notifyObservers(race);


                Thread.sleep(3000);

                String resultSummary = race.startRace(StrategyType.BINARY_SEARCH);


                String finishMsg = "Race '" + race.getName() + "' FINISHED!\n" + resultSummary;
                notifyAllSubscribers(race, finishMsg);
                notifyWithMessage(race, finishMsg);

                eventRepo.delete(race.getID());
                
                notifyObservers(race);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }, executorService);
    }
    private void notifyAllSubscribers(Event event, String message) {
        for (User u : event.getSubscribers()) {
            historyRepo.addHistory(u.getID(), "Event " + event.getName() + ": " + message);
        }
        System.out.println("DEBUG: Notified subscribers of " + event.getName() + ": " + message);
    }
}