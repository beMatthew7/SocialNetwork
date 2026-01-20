package org.example.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.domain.*;
import org.example.interactions.RaceEvent;
import org.example.paging.Page;
import org.example.paging.Pageable;
import org.example.service.*;
import org.example.utils.observer.Observer;
import org.example.utils.observer.RaceNotification;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class DuckController {

    // --- Elemente FXML ---
    @FXML private Label lblUser;
    @FXML private Button btnCommunities;
    @FXML private Button btnMostSociable;
    @FXML private Label lblPage;

    @FXML private ComboBox<String> typeComboBox; // Filtru tip
    @FXML private ComboBox<Integer> comboPageSize; // Paginare

    @FXML private TableView<Duck> duckTableView;
    @FXML private TableColumn<Duck, String> usernameColumn;
    @FXML private TableColumn<Duck, String> emailColumn;
    @FXML private TableColumn<Duck, DuckType> typeColumn;
    @FXML private TableColumn<Duck, Double> speedColumn;
    @FXML private TableColumn<Duck, Double> enduranceColumn;

    @FXML private TableView<User> allUsersTable;
    @FXML private TableColumn<User, String> allUsersNameCol;
    @FXML private TableColumn<User, String> allUsersEmailCol;

    @FXML private TableView<User> friendsTable;
    @FXML private TableColumn<User, String> friendsNameCol;
    @FXML private TableColumn<User, String> friendsEmailCol;

    @FXML private TableView<FriendRequest> friendsRequests;
    @FXML private TableColumn<FriendRequest, String> friendsNameColRequest;
    @FXML private TableColumn<FriendRequest, String> requestStatus;
    private ObservableList<FriendRequest> friendRequestsModel = FXCollections.observableArrayList();

    // --- Servicii si Stare ---
    // --- Servicii si Stare ---
    private UserService userService;
    private FriendshipService friendshipService;
    private CardService cardService;
    private EventService eventService;
    @FXML private TableView<FriendRequest> receivedRequestsTable;
    @FXML private TableColumn<FriendRequest, String> receivedFromUserCol;
    @FXML private TableColumn<FriendRequest, String> receivedStatusCol;
    @FXML private TableColumn<FriendRequest, String> receivedDateCol;
    private ObservableList<FriendRequest> receivedRequestsModel = FXCollections.observableArrayList();

    private org.example.service.MessageService messageService;

    private User currentUser;
    private Stage stage;

    private int currentPage = 1;
    private int pageSize = 5;
    private int totalNumberOfElements = 0;

    private ObservableList<Duck> model = FXCollections.observableArrayList();
    private ObservableList<User> allUsersModel = FXCollections.observableArrayList();
    private ObservableList<User> friendsModel = FXCollections.observableArrayList();

    private org.example.utils.observer.Observer<Duck> duckObserver;
    private org.example.utils.observer.Observer<Friendship> friendshipObserver;
    private org.example.utils.observer.Observer<FriendRequest> requestObserver;
    private FriendRequestService requestService;
    private RaceEventService raceService; // [NEW]

    // --- Initializare ---

    public void setServices(UserService userService, FriendshipService friendshipService,
                            CardService cardService, EventService eventService,
                            org.example.service.MessageService messageService,
                            User currentUser, Stage stage, FriendRequestService requestService,
                            RaceEventService raceService) { // [NEW]
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.cardService = cardService;
        this.eventService = eventService;
        this.messageService = messageService;
        this.currentUser = currentUser;
        this.stage = stage;
        this.stage = stage;
        this.requestService = requestService;
        this.raceService = raceService; // [NEW]

        // Cream si pastram referintele la observeri
        this.duckObserver = e -> {
            initModel();
            updateStats();
        };

        this.friendshipObserver = e -> {
            if (currentUser != null) {
                List<User> friends = friendshipService.getFriendsForUser(currentUser);
                friendsModel.setAll(friends);
                updateStats();
            }
        };

        this.requestObserver = e -> {
            update(e);
        };

        userService.addObserver(duckObserver); // Abonare la modificari
        friendshipService.addObserver(friendshipObserver); // Abonare la modificari prietenii
        requestService.addObserver(requestObserver); // Abonare la friend requests

        if (currentUser != null) {
            lblUser.setText("Logged in as: " + currentUser.getUsername());
        }

        updateStats(); // Calculam statisticile
        initModel();   // Incarcam datele
    }

    @FXML
    public void initialize() {
        // 1. Configurare Coloane Tabel
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        speedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
        enduranceColumn.setCellValueFactory(new PropertyValueFactory<>("endurance"));

        allUsersNameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        allUsersEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        friendsNameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        friendsEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));



        // 2. Configurare Filtru Tip (Optional, daca l-ai pastrat)
        if (typeComboBox != null) {
            typeComboBox.getItems().add("All");
            for (DuckType type : DuckType.values()) {
                typeComboBox.getItems().add(type.name());
            }
            typeComboBox.getSelectionModel().select("All");
            typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> initModel());
        }

        // 3. Configurare Paginare
        comboPageSize.getItems().addAll(2, 5, 10, 20);
        comboPageSize.setValue(pageSize);
        comboPageSize.valueProperty().addListener((obs, oldV, newV) -> {
            this.pageSize = newV;
            this.currentPage = 1;
            initModel();
        });

        duckTableView.setItems(model);
        allUsersTable.setItems(allUsersModel);
        friendsTable.setItems(friendsModel);

        friendsNameColRequest.setCellValueFactory(cellData -> {
            FriendRequest req = cellData.getValue();
            // In tabelul de Sent Requests, aratam CUI am trimis (To)
            return new javafx.beans.property.SimpleStringProperty(req.getTo().getUsername());
        });
        requestStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        friendsRequests.setItems(friendRequestsModel);

        // Configurare Received Requests
        receivedFromUserCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFrom().getUsername()));
        receivedStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        receivedDateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().toString()));
        
        receivedRequestsTable.setItems(receivedRequestsModel);
    }

    private void initModel() {
        // Paginare + Filtrare
        Page<Duck> page;
        String selectedTypeStr = typeComboBox.getSelectionModel().getSelectedItem();

        if (selectedTypeStr == null || selectedTypeStr.equals("All")) {
            page = userService.getAllDucksPaged(new Pageable(currentPage, pageSize));
        } else {
            DuckType type = DuckType.valueOf(selectedTypeStr);
            page = userService.getDucksByTypePaged(type, new Pageable(currentPage, pageSize));
        }

        this.totalNumberOfElements = page.getTotalNumberOfElements();

        List<Duck> duckList = StreamSupport.stream(page.getElementsOnPage().spliterator(), false)
                .collect(Collectors.toList());

        model.setAll(duckList);
        updatePageLabel();

        // Populate All Users
        if (userService != null) {
            // Trebuie convertit Iterable la List
            Iterable<User> usersIterable = userService.getAllUsers();
            List<User> all = StreamSupport.stream(usersIterable.spliterator(), false)
                    .collect(Collectors.toList());
            allUsersModel.setAll(all);
        }

        // Populate Friends
        if (currentUser != null && friendshipService != null) {
            List<User> friends = friendshipService.getFriendsForUser(currentUser);
            friendsModel.setAll(friends);
        }


        //Populate Sent Requests
        if (currentUser != null && requestService != null) {
            List<FriendRequest> sentRequests = requestService.getPendingRequestsForUser(currentUser);
            friendRequestsModel.setAll(sentRequests);

            List<FriendRequest> receivedRequests = requestService.getRequestsToUser(currentUser);
            receivedRequestsModel.setAll(receivedRequests);
        }
    }

    // --- Paginare ---

    @FXML
    public void handleNextPage() {
        int maxPage = (int) Math.ceil((double) totalNumberOfElements / pageSize);
        if (currentPage < maxPage) {
            currentPage++;
            initModel();
        }
    }

    @FXML
    public void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            initModel();
        }
    }

    private void updatePageLabel() {
        int maxPage = (int) Math.ceil((double) totalNumberOfElements / pageSize);
        if (maxPage == 0) maxPage = 1;
        lblPage.setText("Page " + currentPage + " / " + maxPage);
    }

    @FXML
    public void handleShowCommunities() {
        if (friendshipService != null) {
            int nr = friendshipService.getNumberOfCommunities();
            new Alert(Alert.AlertType.INFORMATION, "Number of communities: " + nr).show();
        }
    }

    @FXML
    public void handleShowMostSociable() {
        if (friendshipService != null) {
            List<User> mostSociable = friendshipService.getMostSociableCommunity();
            if (mostSociable.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "No communities found.").show();
            } else {
                String names = mostSociable.stream()
                        .map(User::getUsername)
                        .collect(Collectors.joining("\n"));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Most sociable community");
                alert.setHeaderText("Community members (" + mostSociable.size() + "):");
                alert.setContentText(names);
                alert.show();
            }
        }
    }

    private void updateStats() {
        if (btnCommunities != null) {
            btnCommunities.setText("No. of Communities: Click for details");
        }
        if (btnMostSociable != null) {
            btnMostSociable.setText("Most Sociable: Click for details");
        }
    }

    // --- Functionalitati ---

    @FXML
    public void handleAddFriend() {
        User selectedUser = allUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                requestService.sendRequest(currentUser, selectedUser);
                new Alert(Alert.AlertType.CONFIRMATION, "Friend request sent!").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Select a user from the table!").show();
        }
    }

    @FXML
    public void handleRemoveFriend() {
        User selectedFriend = friendsTable.getSelectionModel().getSelectedItem();
        if (selectedFriend != null) {
            try {
                friendshipService.removeFriend(currentUser, selectedFriend.getUsername());
                new Alert(Alert.AlertType.INFORMATION, "Friend removed successfully!").show();
                updateStats();
                // Reincarcam lista de prieteni
                initModel();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Select a user from the table!").show();
        }
    }

    @FXML
    public void handleOpenChat() {
        User selectedFriend = friendsTable.getSelectionModel().getSelectedItem();
        if (selectedFriend != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gui/chat-view.fxml"));
                Stage chatStage = new Stage();
                chatStage.setScene(new Scene(loader.load()));
                
                ChatController chatController = loader.getController();
                chatController.setService(messageService, currentUser, selectedFriend);
                
                chatStage.setTitle("Chat with " + selectedFriend.getUsername());
                chatStage.show();
                chatStage.setTitle("Chat with " + selectedFriend.getUsername());
                chatStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error opening chat: " + ex.getMessage()).show();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Select a friend to chat with!").show();
        }
    }

    @FXML
    public void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your account?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            try {
                userService.deleteUser(currentUser.getID());
                handleLogout();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        }
    }

    @FXML
    public void handleLogout() throws IOException {
        userService.removeObserver(duckObserver);
        friendshipService.removeObserver(friendshipObserver);
        requestService.removeObserver(requestObserver);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load());

        LoginController controller = loader.getController();

        controller.setServices(userService, friendshipService, cardService, eventService, messageService, stage, requestService, raceService);

        stage.setScene(scene);
        stage.setTitle("Duck Social Network - Login");
    }

    @FXML
    public void handleAcceptRequest() {
        FriendRequest selected = receivedRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getStatus() == RequestStatus.PENDING) {
                requestService.respondToRequest(selected, RequestStatus.APPROVED);
            } else {
                new Alert(Alert.AlertType.WARNING, "Request is not pending!").show();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Select a request to accept!").show();
        }
    }

    @FXML
    public void handleRejectRequest() {
        FriendRequest selected = receivedRequestsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getStatus() == RequestStatus.PENDING) {
                requestService.respondToRequest(selected, RequestStatus.REJECTED);
            } else {
                new Alert(Alert.AlertType.WARNING, "Request is not pending!").show();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Select a request to reject!").show();
        }
    }

    public void update(FriendRequest t) {
        initModel();
        
        if (currentUser != null && requestService != null) {

            if (t.getTo().getID().equals(currentUser.getID()) && t.getStatus() == RequestStatus.PENDING) {
                javafx.application.Platform.runLater(() -> {
                     Alert alert = new Alert(Alert.AlertType.INFORMATION);
                     alert.setTitle("New Friend Request");
                     alert.setHeaderText(null);
                     alert.setContentText("You received a friend request from " + t.getFrom().getUsername() + "!");
                     alert.show();
                });
            }
        }
    }

    // --- RACE EVENT LOGIC ---

    public void createRace(String name, String lanesStr) {
        String[] parts = lanesStr.split(",");
        java.util.List<Integer> lengths = new java.util.ArrayList<>();
        for (String p : parts) {
             try {
                 lengths.add(Integer.parseInt(p.trim()));
             } catch (NumberFormatException e) {
                 // ignore or warn
             }
        }
        if (lengths.isEmpty()) throw new RuntimeException("No valid lane lengths!");

        raceService.createRaceEvent(name, currentUser, lengths);
    }

    public void startRace(RaceEvent race) {
        raceService.startRaceAsync(race.getID(), currentUser);
    }

    public void subscribe(RaceEvent race) {
        eventService.subscribe(race.getID(), currentUser);
    }
    
    public void unsubscribe(RaceEvent race) {
        eventService.unsubscribe(race.getID(), currentUser);
    }

    public List<RaceEvent> getAllRaces() {
        return raceService.getAllRaceEvents();
    }

    public List<RaceEvent> getMyRaces() {
        return raceService.getEventsCreatedBy(currentUser);
    }
    
    public List<String> getNotifications() {
        return eventService.getHistoryForUser(currentUser.getID());
    }

    @FXML
    public void handleOpenEvents() {
        if (raceService == null) {
            new Alert(Alert.AlertType.ERROR, "Race Service not initialized!").show();
            return;
        }
        EventsView eventsView = new EventsView(this, currentUser);
        eventsView.show();
    }
    public void addRaceObserver(Observer<RaceEvent> observer) {
        raceService.addObserver(observer);
    }

    public void addNotificationObserver(Observer<RaceNotification> observer) {
        raceService.addNotificationObserver(observer);
    }

    @FXML
    public void handleOpenMyPage() {
        MyPageView page = new MyPageView(currentUser);
        page.show();
    }
}