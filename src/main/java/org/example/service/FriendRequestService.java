package org.example.service;
import org.example.domain.*;
import org.example.repository.FriendRequestDbRepository;
import org.example.repository.FriendRequestRepository;
import org.example.repository.Repository;
import org.example.utils.observer.Observable;
import org.example.utils.observer.Observer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
public class FriendRequestService implements Observable<FriendRequest> {
    private FriendRequestRepository repo;
    private FriendshipService friendshipService;
    private List<Observer<FriendRequest>> observers = new ArrayList<>();
    public FriendRequestService(FriendRequestRepository repo, FriendshipService friendshipService) {
        this.repo = repo;
        this.friendshipService = friendshipService;
    }
    public void sendRequest(User from, User to) {
        if (friendshipService.areFriends(from.getID(), to.getID())) {
            throw new RuntimeException("Users are already friends!");
        }

        FriendRequest reverseRequest = repo.findOneByUsersPending(to.getID(), from.getID());
        if (reverseRequest != null && reverseRequest.getStatus() == RequestStatus.PENDING) {
            respondToRequest(reverseRequest, RequestStatus.APPROVED);
            return;
        }
        FriendRequest existing = repo.findOneByUsersPending(from.getID(), to.getID());
        if (existing != null && existing.getStatus() == RequestStatus.PENDING) {
            throw new RuntimeException("Request already pending!");
        }

        if (existing != null && existing.getStatus() == RequestStatus.REJECTED) {
            existing.setStatus(RequestStatus.PENDING);
            existing.setDate(java.time.LocalDateTime.now());
            repo.update(existing);
            notifyObservers(existing);
            return;
        }
        FriendRequest request = new FriendRequest(from, to);
        repo.save(request);
        notifyObservers(request);
    }
    public void respondToRequest(FriendRequest request, RequestStatus newStatus) {
        request.setStatus(newStatus);
        repo.update(request);

        if (newStatus == RequestStatus.APPROVED) {
            friendshipService.addFriendship(request.getFrom().getID(), request.getTo().getID());
        }
        notifyObservers(request);
    }
    public List<FriendRequest> getPendingRequestsForUser(User u1) {
        return repo.getRequestForUser(u1.getID());
    }

    public List<FriendRequest> getRequestsToUser(User u1) {
        return repo.getRequestsToUser(u1.getID());
    }

    @Override
    public void addObserver(Observer<FriendRequest> e) { observers.add(e); }
    @Override
    public void removeObserver(Observer<FriendRequest> e) { observers.remove(e); }
    @Override
    public void notifyObservers(FriendRequest t) { observers.forEach(o -> o.update(t)); }
}