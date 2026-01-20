package org.example.service;

import org.example.domain.User;
import org.example.interactions.Event;
import org.example.repository.EventHistoryRepository; // 1. Importa noul repo
import org.example.repository.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pentru gestionarea Evenimentelor.
 * Acum este stateless si deleaga stocarea istoricului catre EventHistoryRepository.
 */
public class EventService {
    private final Repository<Long, Event> eventRepo;
    private final EventHistoryRepository historyRepo; // 2. Deleaga stocarea aici
    private static long nextEventId = 1;

    /**
     * Construieste serviciul de evenimente.
     * @param eventRepo repository pentru entitatile Event
     * @param historyRepo repository pentru stocarea in-memory a istoricului
     */
    public EventService(Repository<Long, Event> eventRepo, EventHistoryRepository historyRepo) {
        this.eventRepo = eventRepo;
        this.historyRepo = historyRepo;

        long maxId = 0;
        for (Event event : eventRepo.findAll()) {
            if (event.getID() != null && event.getID() > maxId) {
                maxId = event.getID();
            }
        }
        nextEventId = maxId + 1;
    }

    public void addEvent(Event event) {
        event.setID(nextEventId++);
        eventRepo.save(event);
    }

    public List<Event> getAll() {
        List<Event> evs = new ArrayList<>();
        eventRepo.findAll().forEach(evs::add);
        return evs;
    }

    public Event findById(long id) {
        return eventRepo.findOne(id);
    }

    public void subscribe(long eventId, User user) {
        Event event = findById(eventId);
        if (event == null) throw new RuntimeException("Evenimentul nu exista!");
        

        boolean alreadySubscribed = event.getSubscribers().stream()
                .anyMatch(u -> u.getID().equals(user.getID()));
        
        if (alreadySubscribed) {
            throw new RuntimeException("Sunteti deja abonat la acest eveniment!");
        }

        event.subscribe(user);
        //historyRepo.addHistory(user.getID(), "Te-ai abonat la evenimentul: " + event.getName());


        eventRepo.update(event);
    }

    public void unsubscribe(long eventId, User user) {
        Event event = findById(eventId);
        if (event != null) {
            boolean isSubscribed = event.getSubscribers().stream()
                .anyMatch(u -> u.getID().equals(user.getID()));
                
            if (!isSubscribed) {
                throw new RuntimeException("Nu sunteti abonat la acest eveniment!");
            }
        
            event.unsubscribe(user);
            //historyRepo.addHistory(user.getID(), "Te-ai dezabonat de la evenimentul: " + event.getName());


            eventRepo.update(event);
        }
    }

    public void deleteEvent(long eventId) {
        eventRepo.delete(eventId);
    }

    public List<Event> getUserSubscriptions(Long userId) {
        List<Event> allEvents = new ArrayList<>();
        eventRepo.findAll().forEach(allEvents::add);

        return allEvents.stream()
                .filter(event ->
                        event.getSubscribers().stream()
                                .anyMatch(user -> user.getID().equals(userId))
                )
                .collect(Collectors.toList());
    }

    /**
     * Inregistreaza un mesaj in istoricul tuturor abonatilor unui eveniment.
     * Este apelat de Consola dupa ce un eveniment (ex. o cursa) se incheie.
     * @param eventId ID-ul evenimentului care s-a incheiat
     * @param message Mesajul rezultat (ex. "Cursa 1 s-a terminat...")
     */
    public void recordHistoryForSubscribers(long eventId, String message) {
        Event event = findById(eventId);
        if (event == null) return;

        for (User subscriber : event.getSubscribers()) {
            Long userId = subscriber.getID();
            historyRepo.addHistory(userId, message);
        }
    }

    /**
     * Returneaza istoricul "live" pentru un utilizator, direct din repository-ul de istoric.
     * @param userId ID-ul utilizatorului
     * @return O lista de string-uri (istoric)
     */
    public List<String> getHistoryForUser(Long userId) {
        return historyRepo.getHistory(userId);
    }
}