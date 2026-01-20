package org.example.gui;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.domain.User;
import org.example.interactions.RaceEvent;
import javafx.application.Platform;
import org.example.utils.observer.Observer;
import org.example.utils.observer.RaceNotification;

public class EventsView implements Observer<RaceEvent> {
    private final DuckController controller;
    private final User currentUser;
    private final Stage stage;
    private TableView<RaceEvent> tblAllEvents;
    private TableView<RaceEvent> tblMyEvents;
    private ListView<String> listNotifications;
    private TextField txtNewEventName;
    private TextField txtLaneLengths;

    private final Observer<RaceNotification> notificationObserver = this::showNotificationPopup;

    public EventsView(DuckController controller, User currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;
        this.stage = new Stage();
        initUI();
        

        controller.addRaceObserver(this);
        controller.addNotificationObserver(notificationObserver);
    }

    private void showNotificationPopup(RaceNotification notification) {
        boolean isSubscribed = notification.getEvent().getSubscribers().stream()
                .anyMatch(u -> u.getID().equals(currentUser.getID()));

        if (isSubscribed) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("🏁 Race Notification");
                alert.setHeaderText(notification.getEvent().getName());
                alert.setContentText(notification.getMessage());
                alert.show();
            });
        }
    }

    @Override
    public void update(RaceEvent event) {
        Platform.runLater(this::refreshData);
    }

    private void initUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setPrefSize(850, 600);

        VBox left = new VBox(10, new Label("Available Events"));
        tblAllEvents = new TableView<>();
        TableColumn<RaceEvent, String> colNameValues = new TableColumn<>("Name");
        colNameValues.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<RaceEvent, String> colCreator = new TableColumn<>("Creator");
        colCreator.setCellValueFactory(cellData -> {
            User u = cellData.getValue().getOrganizer();
            return new javafx.beans.property.SimpleStringProperty(u != null ? u.getUsername() : "Unknown");
        });
        tblAllEvents.getColumns().addAll(colNameValues, colCreator);

        Button btnSub = new Button("Subscribe");
        btnSub.setOnAction(e -> {
            try {
                RaceEvent sel = tblAllEvents.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    controller.subscribe(sel);
                    new Alert(Alert.AlertType.INFORMATION, "Subscribed!").show();
                    refreshData();
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
            }
        });

        Button btnUnsub = new Button("Unsubscribe");
        btnUnsub.setOnAction(e -> {
            try {
                RaceEvent sel = tblAllEvents.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    controller.unsubscribe(sel);
                    new Alert(Alert.AlertType.INFORMATION, "Unsubscribed!").show();
                    refreshData();
                } else {
                    new Alert(Alert.AlertType.WARNING, "Selectati un eveniment!").show();
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
            }
        });

        left.getChildren().addAll(tblAllEvents, new HBox(10, btnSub, btnUnsub));
        

        VBox right = new VBox(10, new Label("My Events"));
        tblMyEvents = new TableView<>();
        TableColumn<RaceEvent, String> colMyName = new TableColumn<>("Name");
        colMyName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tblMyEvents.getColumns().add(colMyName);
        txtNewEventName = new TextField(); txtNewEventName.setPromptText("Name");
        txtLaneLengths = new TextField(); txtLaneLengths.setPromptText("Lengths (100,200)");

        Button btnCreate = new Button("Create");
        btnCreate.setOnAction(e -> {
            try {
                controller.createRace(txtNewEventName.getText(), txtLaneLengths.getText());

                new Alert(Alert.AlertType.INFORMATION, "Created!").show();
                refreshData();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).show();
            }
        });
        Button btnStart = new Button("START RACE");
        btnStart.setOnAction(e -> {
            RaceEvent sel = tblMyEvents.getSelectionModel().getSelectedItem();
            if (sel != null) {
                controller.startRace(sel);
                new Alert(Alert.AlertType.INFORMATION, "Started!").show();
            }
        });
        right.getChildren().addAll(tblMyEvents, txtNewEventName, txtLaneLengths, btnCreate, btnStart);
        // Notificari
        listNotifications = new ListView<>();
        Button btnRefresh = new Button("Refresh History");
        btnRefresh.setOnAction(e -> refreshData());

        root.setLeft(left);
        root.setRight(right);
        root.setBottom(new VBox(5, new Label("History"), listNotifications, btnRefresh));
        stage.setScene(new Scene(root));
        stage.setTitle("Events - " + currentUser.getUsername());
        refreshData();
    }
    public void show() { stage.show(); }
    private void refreshData() {
        tblAllEvents.getItems().setAll(controller.getAllRaces());
        tblMyEvents.getItems().setAll(controller.getMyRaces());
        listNotifications.getItems().setAll(controller.getNotifications());
    }
}