package org.example.gui;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.domain.Message;
import org.example.domain.User;
import org.example.service.MessageService;
import org.example.utils.observer.Observer;
import java.util.Collections;
import java.util.List;
public class ChatController implements Observer<Message> {
    @FXML private ListView<Message> messageList;
    @FXML private TextField messageInput;
    @FXML private Label userLabel;
    @FXML private Label replyPreview;

    private MessageService service;
    private User currentUser;
    private User otherUser;
    private ObservableList<Message> model = FXCollections.observableArrayList();

    public void setService(MessageService service, User currentUser, User otherUser) {
        this.service = service;
        this.currentUser = currentUser;
        this.otherUser = otherUser;
        service.addObserver(this);
        userLabel.setText("Chat with " + otherUser.getUsername());
        initModel();
        
        messageList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                replyPreview.setText("Replying to: " + newVal.getMessage());
            } else {
                replyPreview.setText("");
            }
        });
    }

    private void initModel() {
        List<Message> messages = service.getConversation(currentUser, otherUser);
        model.setAll(messages);
        messageList.setItems(model);
        
        messageList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle(""); 
                } else {
                    HBox container = new HBox();
                    VBox bubble = new VBox();
                    bubble.setStyle("-fx-padding: 10; -fx-background-radius: 10;");
                    
                    if (item.getReply() != null) {
                        Label replyLabel = new Label("Replying to: " + item.getReply().getMessage());
                        replyLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black; -fx-font-style: italic;");
                        bubble.getChildren().add(replyLabel);
                    }
                    
                    Text text = new Text(item.getMessage());
                    text.setStyle("-fx-fill: white;");
                    bubble.getChildren().add(text);

                    if (item.getFrom().getID().equals(currentUser.getID())) {
                        container.setAlignment(Pos.CENTER_RIGHT);
                        bubble.setStyle(bubble.getStyle() + "-fx-background-color: #0084ff;");
                        text.setStyle("-fx-fill: white;");
                    } else {
                        container.setAlignment(Pos.CENTER_LEFT);
                        bubble.setStyle(bubble.getStyle() + "-fx-background-color: #e4e6eb;");
                        text.setStyle("-fx-fill: black;");
                    }
                    
                    container.getChildren().add(bubble);
                    setGraphic(container);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }

    @FXML
    public void handleSendMessage() {
        String text = messageInput.getText();
        if (text.isEmpty()) return;

        Message selected = messageList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.replyMessage(currentUser, selected, text);
            messageList.getSelectionModel().clearSelection();
        } else {
            service.sendMessage(currentUser, Collections.singletonList(otherUser), text);
        }
        messageInput.clear();
    }

    @Override
    public void update(Message message) {
        Platform.runLater(() -> {
            List<Message> messages = service.getConversation(currentUser, otherUser);
            model.setAll(messages);
            messageList.scrollTo(messages.size() - 1);
        });
    }
}