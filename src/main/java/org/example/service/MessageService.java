package org.example.service;
import org.example.domain.Message;
import org.example.domain.User;
import org.example.repository.MessageDbRepository;
import org.example.utils.observer.Observable;
import org.example.utils.observer.Observer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class MessageService implements Observable<Message> {
    private MessageDbRepository repo;
    private List<Observer<Message>> observers = new ArrayList<>();
    public MessageService(MessageDbRepository repo) {
        this.repo = repo;
    }
    public void sendMessage(User from, List<User> to, String text) {
        Message msg = new Message(from, to, text);
        repo.save(msg);
        notifyObservers(msg);
    }

    public void replyMessage(User from, Message original, String text) {
        Message reply = new Message(from, original.getTo(), text);
        reply.setTo(List.of(original.getFrom()));
        reply.setReply(original);
        repo.save(reply);
        notifyObservers(reply);
    }
    public List<Message> getConversation(User u1, User u2) {
        return repo.findConversation(u1.getID(), u2.getID());
    }
    @Override
    public void addObserver(Observer<Message> e) {
        observers.add(e);
    }
    @Override
    public void removeObserver(Observer<Message> e) {
        observers.remove(e);
    }
    @Override
    public void notifyObservers(Message t) {
        observers.forEach(o -> o.update(t));
    }
}