package org.example.ui;

import org.example.domain.*;
import org.example.domain.validators.ValidationException;
import org.example.interactions.Card;
import org.example.interactions.DuckCard;
import org.example.interactions.Event;
import org.example.interactions.RaceEvent;
import org.example.service.CardService;
import org.example.service.EventService;
import org.example.service.FriendshipService;
import org.example.service.UserService;
import org.example.strategy.StrategyType;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;

public class Console {

    private static UserService userService = null;
    private static FriendshipService friendshipService = null;
    private static CardService cardService = null;
    private static EventService eventService = null;
    private static Scanner scanner = null;
    private static User currentUser = null;

    public Console(UserService userService, FriendshipService friendshipService,
                   CardService cardService, EventService eventService) {
        Console.userService = userService;
        Console.friendshipService = friendshipService;
        Console.cardService = cardService;
        Console.eventService = eventService;
        scanner = new Scanner(System.in);
    }
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Trebuie să introduceti o valoare întreaga valida!");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Trebuie să introduceti o valoare numerica valida!");
            }
        }
    }
    private static long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Trebuie să introduceti o valoare numerica valida!");
            }
        }
    }

    static void showMenu(){
        System.out.println("1. Creare utilizator");
        System.out.println("2. Modificare utilizator");
        System.out.println("3. Stergere utilizator");
        System.out.println("4. Listare utilizatori");
        System.out.println("5. Logare");
        System.out.println("6. Nuamrul de comunitati");
        System.out.println("7. Cea mai sociabila comunitate");
        System.out.println("8. Creare card");
        System.out.println("9. Stergere Card");
        System.out.println("10. Creeaza Event");
        System.out.println("11. Run event");
        System.out.println("12. Listeaza evenimente");
        System.out.println("E. Iesire");
    }

    private static void showMenuLoggedPerson(){
        System.out.println("1. Adauga prieten");
        System.out.println("2. Sterge Prieten");
        System.out.println("3. Cauta utilizator dupa nume");
        System.out.println("4. Listare Prieteni");
        System.out.println("5. Istoric Evenimente");
        System.out.println("6. Subscribe la eveniment");
        System.out.println("7. Unsubscribe de la eveniment");
        System.out.println("E. Delogare");
    }
    private static void showMenuLoggedDuck() {
        System.out.println("1. Adauga prieten");
        System.out.println("2. Sterge Prieten");
        System.out.println("3. Cauta utilizator dupa nume");
        System.out.println("4. Listare prieteni");
        System.out.println("5. Vezi carduri disponibile");
        System.out.println("6. Intra in card");
        System.out.println("7. Vezi cardul meu");
        System.out.println("8. Iesi din card");
        System.out.println("9. Istotic evenimente");
        System.out.println("E. Delogare");
    }

    static void addUserUI(){
        System.out.println("Tip utilizator: (1=Person, 2=Duck): ");
        String tip = scanner.nextLine();

        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (tip.equals("1")) {
            System.out.print("Prenume: ");
            String firstName = scanner.nextLine();
            System.out.print("Nume: ");
            String secondName = scanner.nextLine();
            System.out.print("Data nasterii(dd.mm.yyyy): ");
            String data = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate localDate = LocalDate.parse(data, formatter);
            Date dateOfBirth = java.sql.Date.valueOf(localDate);
            System.out.print("Ocupatie: ");
            String occupation = scanner.nextLine();
            int empathyNivel = 0;
            while (true) {
                System.out.print("Nivel empatie: ");
                String empathyInput = scanner.nextLine();
                try {
                    empathyNivel = Integer.parseInt(empathyInput);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Introduceți un numar valid pentru nivelul de empatie!");
                }
            }

            try {
                Person p = new Person(username, email, password, firstName, secondName, dateOfBirth, occupation, empathyNivel);
                Person saved = userService.createPerson(p);
                if (saved == null) {
                    System.out.println("Utilizator creat cu succes!");
                } else {
                    System.out.println("Eroare: Utilizatorul cu acest ID/Username/Email exista deja!");
                }
            } catch (ValidationException e) {
                System.out.println("Eroare la creare: " + e.getMessage());
            }
        } else if (tip.equals("2")) {
            System.out.print("Tip rata (FLYING, SWIMMING, FLYING_AND_SWIMMING): ");
            String duckType = scanner.nextLine().trim().toUpperCase();
            double speed = readDouble("Viteza: ");
            double endurance = readDouble("Rezistență: ");

            try {
                DuckType type = DuckType.valueOf(duckType);
                Duck d;
                if (type == DuckType.FLYING) {
                    d = new FlyingDuck(username, email, password, type, speed, endurance);
                } else if (type == DuckType.SWIMMING) {
                    d = new SwimmingDuck(username, email, password, type, speed, endurance);
                }else{
                    d = new FlyingSwimmingDuck(username, email, password, type, speed, endurance);
                }

                Duck saved = userService.createDuck(d);
                if (saved == null) {
                    System.out.println("Rață creată cu succes!");
                } else {
                    System.out.println("Eroare: Rața cu acest ID/Username/Email exista deja!");
                }
            } catch (ValidationException e) {
                System.out.println("Eroare la creare: " + e.getMessage());
            }
        }

    }

    private void listAllUserUI(){
        for (Person p : userService.getAllPeople()) {
            System.out.println(p);
        }

        for(Duck d : userService.getAllDucks()){
            System.out.println(d);
        }


    }

    private static void loginUI(){
        System.out.println("Introduceti username ul: ");
        String username = scanner.nextLine();

        System.out.println("Introdueti parola dumneavoastra");
        String password  = scanner.nextLine();

        try {
            User user = userService.login(username, password);
            currentUser = user;
            System.out.println("Autentificare reușită!");
            runLogged();
        } catch (RuntimeException e) {
            System.out.println("Eroare la autentificare: " + e.getMessage());
        }

    }

    private static void searchUserNameUI(){
        System.out.println("Intrdocueti numele: ");
        String name = scanner.nextLine();

        for(User u: userService.searchUserName(name)){
            System.out.println(u.getUsername());
        }
    }

    private static void deleteUserUI(){
        String prompt = "Introduceti id ul utilizatorului: ";
        long id = readLong(prompt);

        try{
            userService.deleteUser(id);
            System.out.println("Utilizator sters cu succes");
        }catch (Exception e){
            System.out.println(e);
        }



    }

    private static void addFriendUI() {
        System.out.println("Introduceți username-ul prietenului pe care vrei să-l adaugi:");
        String username = scanner.nextLine();

        if (currentUser == null) {
            System.out.println("Nu ești autentificat!");
            return;
        }

        try {
            friendshipService.addFriend(currentUser, username);
            System.out.println("Prieten adăugat cu succes!");
        } catch (RuntimeException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void deleteFriendUI() {
        System.out.println("Introduceți username-ul prietenului pe care vrei să-l ștergi:");
        String username = scanner.nextLine();

        if (currentUser == null) {
            System.out.println("Nu ești autentificat!");
            return;
        }
        try {
            friendshipService.removeFriend(currentUser, username);
            System.out.println("Prieten șters cu succes!");
        } catch (RuntimeException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void listFriendsUI() {
        if (currentUser == null) {
            System.out.println("Nu ești autentificat!");
            return;
        }
        List<User> friends = friendshipService.getFriendsForUser(currentUser);
        if (friends == null || friends.isEmpty()) {
            System.out.println("Nu ai niciun prieten adăugat.");
            return;
        }
        System.out.println("Lista ta de prieteni:");
        for (User friend : friends) {
            System.out.println("- " + friend.getUsername());
        }
    }

    private static void numberOfCommunitiesUI() {
        int nr = friendshipService.getNumberOfCommunities();
        System.out.println("Numarul de comunitați: " + nr);
    }

    private static void mostSociableCommunityUI() {
        List<User> community = friendshipService.getMostSociableCommunity();
        System.out.println("Cea mai sociabila comunitate are " + community.size() + " membri:");
        for (User u : community) {
            System.out.println("- " + u.getUsername());
        }
    }
    private static void createCardUI() {
        System.out.print("Nume card: ");
        String cardName = scanner.nextLine();

        System.out.println("Tip card (pentru ce tip de rate):");
        System.out.println("1. FLYING");
        System.out.println("2. SWIMMING");
        System.out.println("3. FLYING_AND_SWIMMING");
        System.out.print("Alegere: ");
        String choice = scanner.nextLine();

        DuckType targetType;
        if (choice.equals("1")) {
            targetType = DuckType.FLYING;
        } else if (choice.equals("2")) {
            targetType = DuckType.SWIMMING;
        } else if (choice.equals("3")) {
            targetType = DuckType.FLYING_AND_SWIMMING;
        } else {
            System.out.println("Optiune invalida!");
            return;
        }

        try {
            DuckCard card = cardService.createCard(cardName, targetType);
            System.out.println("Card creat cu succes! ID: " + card.getID() +
                    " - Nume: " + card.getCardName() +
                    " - Pentru: " + card.getTargetType());
        } catch (RuntimeException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }
    private static void viewAvailableCardsUI() {
        if (!(currentUser instanceof Duck)) {
            System.out.println("Doar ratele pot vedea carduri!");
            return;
        }

        Duck duck = (Duck) currentUser;
        List<DuckCard> cards = cardService.getAvailableCardsForDuck(duck);

        if (cards.isEmpty()) {
            System.out.println("Nu exista carduri disponibile pentru tipul tau de rata (" + duck.getType() + ")");
        } else {
            System.out.println("Carduri disponibile pentru tine (" + duck.getType() + "):");
            for (DuckCard card : cards) {
                System.out.println("ID: " + card.getID() +
                        " - " + card.getCardName() +
                        " (pentru " + card.getTargetType() +
                        ") - Membri: " + card.getMembers().size());
            }
        }
    }

    private static void joinCardUI() {
        if (!(currentUser instanceof Duck)) {
            System.out.println("Doar ratele pot intra in carduri!");
            return;
        }

        Duck duck = (Duck) currentUser;

        DuckCard currentCard = cardService.getCardForDuck(duck);
        if (currentCard != null) {
            System.out.println("Esti deja in cardul: " + currentCard.getCardName());
            System.out.println("Trebuie sa iesi mai intai!");
            return;
        }


        List<DuckCard> availableCards = cardService.getAvailableCardsForDuck(duck);

        if (availableCards.isEmpty()) {
            System.out.println("Nu exista carduri disponibile pentru tipul tau de rata (" + duck.getType() + ")");
            return;
        }

        System.out.println("Carduri disponibile:");
        for (DuckCard card : availableCards) {
            System.out.println("ID: " + card.getID() +
                    " - " + card.getCardName() +
                    " (pentru " + card.getTargetType() +
                    ") - Membri: " + card.getMembers().size());
        }

        Long cardId = readLong("Introduceti ID card: ");

        try {
            cardService.joinCard(cardId, duck);
            System.out.println("Te-ai inrolat cu succes in cardul: " +
                    cardService.findCardById(cardId).getCardName() + "!");
        } catch (RuntimeException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }
    private static void viewMyCardUI() {
        if (!(currentUser instanceof Duck)) {
            System.out.println("Doar ratele au carduri!");
            return;
        }

        Duck duck = (Duck) currentUser;
        DuckCard card = cardService.getCardForDuck(duck);
        if (card == null) {
            System.out.println("Nu esti in niciun card.");
        } else {

            System.out.println("Nume: " + card.getCardName());
            System.out.println("Tip: " + card.getTargetType());
            System.out.println("Numar membri: " + card.getMembers().size());
            System.out.println("Performanta medie: " + String.format("%.2f", card.getAveragePerformance()));

            System.out.println("\nMembri:");
            if (card.getMembers().isEmpty()) {
                System.out.println("Niciun membru.");
            } else {
                for (Duck member : card.getMembers()) {
                    System.out.println("- " + member.getUsername() +
                            " (" + member.getType() + ")");
                }
            }
        }
    }

    private static void leaveCardUI() {
        if (!(currentUser instanceof Duck)) {
            System.out.println("Doar ratele pot iesi din carduri!");
            return;
        }

        Duck duck = (Duck) currentUser;

        try {
            cardService.leaveCard(duck);
            System.out.println("Ai iesit din card cu succes!");
        } catch (RuntimeException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void deleteCardUI() {
        System.out.println("Carduri existente:");
        List<DuckCard> cards = cardService.getAllCards();
        if (cards.isEmpty()) {
            System.out.println("Nu exista carduri!");
            return;
        }
        for (DuckCard card : cards) {
            System.out.println("ID: " + card.getID() + " - " + card.getCardName() +
                    " (pentru " + card.getTargetType() + ")");
        }

        Long cardId = readLong("Introduceti ID card de sters: ");

        try {
            cardService.deleteCard(cardId);
            System.out.println("Card sters cu succes!");
        } catch (RuntimeException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void historyEventUI(){
        if(currentUser == null){
            System.out.println("Nu este nimeni conectat");
            return;
        }
        if(eventService.getHistoryForUser(currentUser.getID()).isEmpty()){
            System.out.println("Nu exista evenimente anterioare");
            return;
        }
        for(String s: eventService.getHistoryForUser(currentUser.getID())){
            System.out.println(s);
        }
    }
    private static boolean requireLoggedIn() {
        if (currentUser == null) {
            System.out.println("Trebuie sa fii autentificat.");
            return false;
        }
        return true;
    }

    private static List<Event> getMySubscriptionsOrWarn() {
        if (!requireLoggedIn()) return java.util.Collections.emptyList();
        List<Event> subs = eventService.getUserSubscriptions(currentUser.getID());
        if (subs == null || subs.isEmpty()) {
            System.out.println("Nu esti abonat la niciun eveniment.");
            return java.util.Collections.emptyList();
        }
        return subs;
    }

    private static void printEventsList(java.util.List<Event> events) {
        for (Event e : events) {
            System.out.println("[" + e.getID() + "] " + e.getName());
        }
    }
    private static void subscribeToEventUI() {
        if (!requireLoggedIn()) return;

        listEventsUI();
        long id = readLong("Introdu event id ca sa dai subscribe: ");
        eventService.subscribe(id, currentUser);
        System.out.println("Subscribed.");
    }

    private static void listMySubscriptionsUI() {
        var subs = getMySubscriptionsOrWarn();
        if (subs.isEmpty()) return;

        System.out.println("Your subscriptions:");
        printEventsList(subs);
    }

    private static void unsubscribeEventUI() {
        var subs = getMySubscriptionsOrWarn();
        if (subs.isEmpty()) return;

        printEventsList(subs);
        long id = readLong("Introdu event id ca sa dai unsubscribe: ");

        boolean isMine = subs.stream().anyMatch(ev -> ev.getID() == id);
        if (!isMine) {
            System.out.println("Nu esti abonat la acest eveniment.");
            return;
        }

        eventService.unsubscribe(id, currentUser);
        System.out.println("Unsubscribed.");
    }


    public static void runLogged(){
        while(true){

            if (currentUser instanceof Duck) {
                showMenuLoggedDuck();
            } else if (currentUser instanceof Person) {
                showMenuLoggedPerson();
            } else {

                showMenuLoggedPerson();
            }

            System.out.print("Introduceti optiunea: ");
            String optiune = scanner.nextLine();


            if (currentUser instanceof Duck) {
                switch (optiune) {
                    case "1":
                        addFriendUI();
                        break;
                    case "2":
                        deleteFriendUI();
                        break;
                    case "3":
                        searchUserNameUI();
                        break;
                    case "4":
                        listFriendsUI();
                        break;
                    case "5":
                        viewAvailableCardsUI();
                        break;
                    case "6":
                        joinCardUI();
                        break;
                    case "7":
                        viewMyCardUI();
                        break;
                    case "8":
                        leaveCardUI();
                        break;
                    case "E":
                        System.out.println("La revedere!");
                        currentUser = null;
                        return;
                    default:
                        System.out.println("Optiune invalida!");
                }
            }

            else if (currentUser instanceof Person) {
                switch (optiune) {
                    case "1":
                        addFriendUI();
                        break;
                    case "2":
                        deleteFriendUI();
                        break;
                    case "3":
                        searchUserNameUI();
                        break;
                    case "4":
                        listFriendsUI();
                        break;
                    case "5":
                        historyEventUI();
                        break;
                    case "6":
                        subscribeToEventUI();
                        break;
                    case "7":
                        unsubscribeEventUI();
                        break;
                    case "E":
                        System.out.println("La revedere!");
                        currentUser = null;
                        return;
                    default:
                        System.out.println("Optiune invalida!");
                }
            }

            System.out.println();
        }
    }
    private static void createRaceEventUI() {
//        System.out.print("Event name: ");
//        String name = scanner.nextLine();
//
//        int m = readInt("Numar linii (M): ");
//        List<Lane> lanes = new ArrayList<>();
//        for (int i = 1; i <= m; i++) {
//            int len = (int) readDouble("Lane " + i + " length (meters): ");
//            lanes.add(new Lane(len));
//        }
//
//        //RaceEvent race = new RaceEvent(name);
//
//
//        List<Duck> ducks = new ArrayList<>();
//        for (Duck d : userService.getAllDucks()) ducks.add(d);
//        race.autoSelectParticipants(ducks, lanes);
//
//        eventService.addEvent(race);
//
//        System.out.println("S a creat evenimentul cu id=" + race.getID());
        System.out.print("Nume eveniment: ");

    }
    private static void runRaceEventUI() {
        listEventsUI();
        long id = readLong("Introdu id ul evenimentului: ");
        Event e = eventService.findById(id);
        if (!(e instanceof RaceEvent)) { System.out.println("Nu exista acest eveniment."); return; }
        RaceEvent race = (RaceEvent) e;


        System.out.println("Strategie (1=BINARY_SEARCH, 2=BACKTRACKING): ");
        String s = scanner.nextLine().trim();
        StrategyType type = "2".equals(s) ? StrategyType.BACKTRACKING : StrategyType.BINARY_SEARCH;

        String raceResult = race.startRace(type);

        System.out.println(raceResult);

        eventService.recordHistoryForSubscribers(race.getID(), raceResult);

        System.out.println("Cursa a fost executata. Subscribers au fost notificati (vedeti istoricul).");

        eventService.deleteEvent(id);
    }

    private static void listEventsUI() {
        List<Event> evs = eventService.getAll();
        if (evs.isEmpty()) { System.out.println("Nu sunt evenimente"); return; }
        for (Event ev : evs) {
            System.out.println("[" + ev.getID() + "] " + ev.getName());
        }
    }

    public void run() {
        while (true) {
            showMenu();

            System.out.print("Introduceti optiunea: ");

            String optiune = scanner.nextLine();

            switch (optiune) {
                case "1":
                    addUserUI();
                    break;
                case "2":
                    break;
                case "3":
                    deleteUserUI();
                    break;
                case "4":
                    listAllUserUI();
                    break;
                case "5":
                    loginUI();
                    break;
                case "6":
                    numberOfCommunitiesUI();
                    break;
                case "7":
                    mostSociableCommunityUI();
                    break;
                case "8":
                    createCardUI();
                    break;
                case "9":
                    deleteCardUI();
                    break;
                case "10":
                    createRaceEventUI();
                    break;
                case "11":
                    runRaceEventUI();
                    break;
                case "12":
                    listEventsUI();
                    break;
                case "E":
                    System.out.println("La revedere!");
                    currentUser = null;
                    return;
                default:
                    System.out.println("Optiune invalida!");
            }
            System.out.println();
        }
    }
}
