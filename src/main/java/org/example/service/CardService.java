package org.example.service;

import org.example.domain.Duck;
import org.example.domain.DuckType;
import org.example.interactions.DuckCard;
import org.example.repository.CardMembershipRepository;
import org.example.repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CardService {
    private final Repository<Long, DuckCard> cardRepo;
    private final Repository<Long, Duck> duckRepo;
    private final CardMembershipRepository membershipRepo; // Avem nevoie de el!

    private static long nextCardId = 1;

    // Constructor cu 3 argumente (așa cum îl apelează MainApp)
    public CardService(Repository<Long, DuckCard> cardRepo, Repository<Long, Duck> duckRepo, CardMembershipRepository membershipRepo){
        this.cardRepo = cardRepo;
        this.duckRepo = duckRepo;
        this.membershipRepo = membershipRepo;

        long maxId = 0;
        for (DuckCard card : cardRepo.findAll()) {
            if (card.getID() != null && card.getID() > maxId) {
                maxId = card.getID();
            }
        }
        nextCardId = maxId + 1;
    }

    // Încarcă membrii folosind tabela de legătură card_memberships
    private void loadMembersForCard(DuckCard card) {
        if (card == null) return;

        // 1. Luăm ID-urile din membershipRepo (care citește din DB sau memorie)
        Set<Long> memberIds = membershipRepo.getMemberIds(card.getID());

        // 2. Încărcăm obiectele Duck
        for (Long duckId : memberIds) {
            Duck duck = duckRepo.findOne(duckId);
            if (duck != null) {
                card.addMember(duck);
                duck.setCard(card);
            }
        }
    }

    public DuckCard createCard(String cardName, DuckType targetType){
        DuckCard card = new DuckCard(cardName, targetType);
        card.setID(nextCardId++);
        DuckCard result = cardRepo.save(card);
        return result == null ? card : result;
    }

    public void joinCard(Long cardId, Duck duck){
        DuckCard card = cardRepo.findOne(cardId);

        if(card == null){
            throw new RuntimeException("Card nu exista");
        }

        loadMembersForCard(card);

        if(!card.canJoin(duck)){
            throw new RuntimeException("Rata ta (" + duck.getType() +
                    ") nu poate intra in acest card (pentru " + card.getTargetType() + ")");
        }

        // Verificăm dacă e deja membru (după ID)
        for(Duck member : card.getMembers()) {
            if(member.getID().equals(duck.getID())) {
                throw new RuntimeException("Rata este deja in acest card");
            }
        }

        if(getCardForDuck(duck) != null){
            throw new RuntimeException("Rata este deja membra altui card");
        }

        // Modificări în memorie
        card.addMember(duck);
        duck.setCard(card);

        // Modificări în Baza de Date (tabela de legătură)
        membershipRepo.saveMembership(cardId, duck.getID());

        // Opțional: dacă ai FK în tabela ducks, actualizează și acolo
        // duckRepo.update(duck);
    }

    public void leaveCard(Duck duck){
        DuckCard card = getCardForDuck(duck);

        if(card == null){
            throw new RuntimeException("Rata nu aparine niciunui card");
        }

        card.removeMember(duck);
        duck.setCard(null);

        // Ștergem din tabela de legătură
        membershipRepo.deleteMembership(card.getID(), duck.getID());

        // Opțional: duckRepo.update(duck);
    }

    public List<DuckCard> getAvailableCardsForDuck(Duck duck){
        List<DuckCard> available = new ArrayList<>();
        for(DuckCard card: cardRepo.findAll()){
            loadMembersForCard(card);
            if(card.canJoin(duck)){
                available.add(card);
            }
        }
        return available;
    }

    public List<DuckCard> getAllCards(){
        List<DuckCard> cards = new ArrayList<>();
        for (DuckCard card: cardRepo.findAll()){
            loadMembersForCard(card);
            cards.add(card);
        }
        return cards;
    }

    public DuckCard findCardById(Long id){
        DuckCard card = cardRepo.findOne(id);
        loadMembersForCard(card);
        return card;
    }

    public void deleteCard(Long cardId){
        DuckCard card = cardRepo.findOne(cardId);
        if(card == null){
            throw new RuntimeException("Acest card nu exista");
        }
        cardRepo.delete(cardId);
    }

    public DuckCard getCardForDuck(Duck duck) {
        // Folosim membershipRepo pentru a găsi rapid cardul
        Long cardId = membershipRepo.getCardIdForDuck(duck.getID());

        if (cardId != null) {
            DuckCard card = cardRepo.findOne(cardId);
            loadMembersForCard(card);
            return card;
        }
        return null;
    }
}