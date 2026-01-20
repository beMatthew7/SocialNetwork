package org.example.service;

import org.example.domain.*; // Importam tot
import org.example.repository.FriendshipRepo;
import org.example.repository.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;

public class FriendshipService implements org.example.utils.observer.Observable<Friendship> {

    private Repository<Long, Person> personRepo;
    private Repository<Long, Duck> duckRepo;
    private FriendshipRepo friendshipRepo;
    private AtomicLong maxFriendshipId;

    private List<org.example.utils.observer.Observer<Friendship>> observers = new ArrayList<>();

    @Override
    public void addObserver(org.example.utils.observer.Observer<Friendship> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(org.example.utils.observer.Observer<Friendship> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(Friendship t) {
        observers.forEach(o -> o.update(t));
    }

    public FriendshipService(Repository<Long, Person> personRepo, Repository<Long, Duck> duckRepo, FriendshipRepo friendshipRepo) {
        this.personRepo = personRepo;
        this.duckRepo = duckRepo;
        this.friendshipRepo = friendshipRepo;

        // Initializam generatorul de ID-uri
        this.maxFriendshipId = new AtomicLong(findMaxId());
    }

    private Long findMaxId() {
        long max = 0L;
        for (Friendship f : friendshipRepo.findAll()) {
            if (f.getID() > max) {
                max = f.getID();
            }
        }
        return max;
    }

    private Long getNextFriendshipId() {
        return maxFriendshipId.incrementAndGet();
    }


    /**
     * Gaseste un utilizator (Person sau Duck) dupa username.
     * @param username Username-ul de cautat
     * @return Obiectul User sau null daca nu e gasit
     */
    public User findUserByUsername(String username) {
        // Iteram prin ambele repository-uri
        for (Person p : personRepo.findAll()) {
            if (p.getUsername().equals(username)) {
                return p;
            }
        }
        for (Duck d : duckRepo.findAll()) {
            if (d.getUsername().equals(username)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Verifica daca doi utilizatori sunt deja prieteni.
     * @param id1 ID-ul primului utilizator
     * @param id2 ID-ul celui de-al doilea utilizator
     * @return true daca sunt prieteni, false altfel
     */
    public boolean areFriends(Long id1, Long id2) {
        return friendshipRepo.areFriends(id1, id2);
    }


    /**
     * Adauga o prietenie, pornind de la un username.
     * Aceasta este metoda apelata de UI.
     * @param currentUser Utilizatorul logat
     * @param friendUsername Username-ul prietenului de adaugat
     * @throws RuntimeException daca utilizatorul nu e gasit, e acelasi user, sau sunt deja prieteni
     */
    public void addFriend(User currentUser, String friendUsername) {
        User friend = findUserByUsername(friendUsername);

        if (friend == null) {
            throw new RuntimeException("Utilizatorul '" + friendUsername + "' nu a fost gasit!");
        }

        Long id1 = currentUser.getID();
        Long id2 = friend.getID();

        if (id1.equals(id2)) {
            throw new RuntimeException("Nu te poti adauga singur ca prieten!");
        }

        if (areFriends(id1, id2)) {
            throw new RuntimeException("Sunteti deja prieteni!");
        }

        // Daca toate verificarile trec, apelam metoda interna
        boolean success = addFriendship(id1, id2);
        if (!success) {
            throw new RuntimeException("A aparut o eroare la salvarea prieteniei.");
        }
    }

    /**
     * Metoda interna care creeaza si salveaza prietenia.
     */
    public boolean addFriendship(Long id1, Long id2) {
        Long newId = getNextFriendshipId();
        long timestamp = System.currentTimeMillis();
        Friendship friendship = new Friendship(newId, id1, id2, timestamp); // Folosim constructorul tau

        Friendship result = friendshipRepo.save(friendship);
        if (result == null) {
            notifyObservers(friendship);
            return true;
        }
        return false;
    }

    /**
     * Sterge o prietenie, pornind de la un username.
     * (Numele "removeFriend" se potriveste cu UI-ul tau)
     * @param currentUser Utilizatorul logat
     * @param friendUsername Username-ul prietenului de sters
     */
    public void removeFriend(User currentUser, String friendUsername) {
        User friend = findUserByUsername(friendUsername);
        if (friend == null) {
            throw new RuntimeException("Utilizatorul '" + friendUsername + "' nu a fost gasit!");
        }

        Long id1 = currentUser.getID();
        Long id2 = friend.getID();

        // Apelam metoda interna de stergere
        boolean success = deleteFriendship(id1, id2);
        if (!success) {
            throw new RuntimeException("Prietenia nu a fost gasita sau nu a putut fi stearsa.");
        }
    }

    /**
     * Metoda interna care gaseste si sterge prietenia.
     */
    private boolean deleteFriendship(Long id1, Long id2) {
        Friendship found = null;
        for (Friendship f : friendshipRepo.findAll()) {
            if ( (f.getUserId1().equals(id1) && f.getUserId2().equals(id2)) ||
                    (f.getUserId1().equals(id2) && f.getUserId2().equals(id1)) ) {
                found = f;
                break;
            }
        }

        if (found != null) {
            Friendship result = friendshipRepo.delete(found.getID());
            if (result != null) {
                notifyObservers(result);
                return true;
            }
        }
        return false;
    }

    /**
     * Returneaza lista de prieteni pentru un utilizator dat.
     * Aceasta metoda este optimizata si foloseste filtrarea in baza de date.
     * @param user Utilizatorul pentru care se cauta prietenii
     * @return O lista de obiecte User (Person sau Duck)
     */
    public List<User> getFriendsForUser(User user) {
        if (friendshipRepo instanceof org.example.repository.FriendshipDbRepository) {
            return ((org.example.repository.FriendshipDbRepository) friendshipRepo).findFriendsForUser(user.getID());
        }

        // Fallback pentru alte implementari de repository (ex: in-memory, file)
        Long userId = user.getID();
        List<Friendship> friendships = ((org.example.repository.FriendshipRepo) friendshipRepo).findAllByUserId(userId);

        return friendships.stream()
                .map(f -> f.getUserId1().equals(userId) ? f.getUserId2() : f.getUserId1())
                .map(this::findById)
                .collect(Collectors.toList());
    }

    /**
     * Metoda ajutatoare pentru a gasi un User (Person sau Duck) dupa ID.
     */
    public User findById(Long id) {
        User user = personRepo.findOne(id);
        if (user == null) {
            user = duckRepo.findOne(id);
        }
        return user;
    }
    /**
     * Construieste o lista cu toti utilizatorii (persoane si rate).
     * @return lista tuturor utilizatorilor
     */
    private List<User> getAllUsers(){
        List<User> allUser = new ArrayList<>();
        for(Person p: personRepo.findAll()) allUser.add(p);
        for(Duck d: duckRepo.findAll()) allUser.add(d);
        return allUser;
    }

    /**
     * Calculeaza numarul de comunitati (componente conexe) in graful de prietenii.
     * @return numarul de comunitati
     */
    public int getNumberOfCommunities() {
        Set<User> visited = new HashSet<>();
        int count = 0;
        for (User u : getAllUsers()) {
            if (!visited.contains(u)) {
                dfs(u, visited);
                count++;
            }
        }
        return count;
    }

    /**
     * Gaseste comunitatea cea mai sociabila, definita ca aceea cu diametrul maxim (in termeni de distante BFS).
     * @return lista utilizatorilor ce apartin comunitatii cu diametrul maxim
     */
    public List<User> getMostSociableCommunity() {
        Set<User> visited = new HashSet<>();
        List<User> maxCommunity = new ArrayList<>();
        int maxDiameter = -1;

        List<User> allUsers = getAllUsers();

        for (User user : allUsers) {
            if (!visited.contains(user)) {
                List<User> community = new ArrayList<>();
                collectCommunity(user, community, visited);
                int diameter = getCommunityDiameter(community);
                if (diameter > maxDiameter) {
                    maxDiameter = diameter;
                    maxCommunity = new ArrayList<>(community);
                }
            }
        }
        return maxCommunity;
    }

    /**
     * Parcurgere DFS pentru a marca utilizatorii vizitati dintr-o comunitate.
     * @param user utilizatorul de start
     * @param visited multimea de utilizatori deja vizitati
     */
    private void dfs(User user, Set<User> visited) {
        visited.add(user);

        // --- CORECTURA ---
        // Folosim metoda "live" getFriendsForUser, nu user.getFriends()
        for (User friend : getFriendsForUser(user)) {
            if (!visited.contains(friend)) {
                dfs(friend, visited);
            }
        }
    }

    /**
     * Colecteaza toti utilizatorii conectati (componenta conexa) pornind de la un user dat.
     * @param user utilizatorul de start
     * @param community lista in care se aduna utilizatorii din comunitate
     * @param visited multimea de utilizatori deja vizitati
     */
    private void collectCommunity(User user, List<User> community, Set<User> visited) {
        visited.add(user);
        community.add(user);

        // --- CORECTURA ---
        // Folosim metoda "live" getFriendsForUser, nu user.getFriends()
        for (User friend : getFriendsForUser(user)) {
            if (!visited.contains(friend)) {
                collectCommunity(friend, community, visited);
            }
        }
    }

    /**
     * Calculeaza diametrul (cea mai mare distanta minima intre doua noduri) al unei comunitati.
     * @param community lista utilizatorilor din comunitate
     * @return diametrul comunitatii
     */
    private int getCommunityDiameter(List<User> community) {
        int maxDist = 0;
        for (User u : community) {
            Map<User, Integer> dist = bfsDistances(u, community);
            for (int d : dist.values()) {
                if (d > maxDist) maxDist = d;
            }
        }
        return maxDist;
    }

    /**
     * Calculeaza distantele minime de la un utilizator de start catre restul nodurilor din comunitate folosind BFS.
     * @param start utilizatorul de start
     *• @param community lista utilizatorilor din comunitate
     * @return map de la utilizator la distanta minima in muchii
     */
    private Map<User, Integer> bfsDistances(User start, List<User> community) {
        Map<User, Integer> dist = new HashMap<>();
        Queue<User> queue = new LinkedList<>();
        Set<User> visited = new HashSet<>();
        queue.add(start);
        dist.put(start, 0);
        visited.add(start);

        while (!queue.isEmpty()) {
            User current = queue.poll();

            // --- CORECTURA ---
            // Folosim metoda "live" getFriendsForUser, nu current.getFriends()
            for (User friend : getFriendsForUser(current)) {
                if (community.contains(friend) && !visited.contains(friend)) {
                    dist.put(friend, dist.get(current) + 1);
                    queue.add(friend);
                    visited.add(friend);
                }
            }
        }
        return dist;
    }
}