package org.example.service;

import org.example.domain.Duck;
import org.example.domain.Person;
import org.example.domain.User;
import org.example.paging.Page;
import org.example.paging.Pageable;
import org.example.repository.PagingRepository;
import org.example.repository.Repository;
import org.example.repository.DuckRepo;
import org.example.domain.DuckType;
import org.example.utils.PasswordHasher;


import java.util.*;

/**
 * Service pentru gestionarea utilizatorilor (Person si Duck).
 * Agrega doua repository-uri si expune operatii comune pentru creare,
 * autentificare, cautare, stergere si analize pe graful de prietenii.
 *
 * Note:
 * - ID-urile sunt generate incremental pe baza celui mai mare ID existent in repo-uri.
 * - Implementarea nu este thread-safe; pentru utilizare concurenta, sincronizati accesul la generateNextId().
 */
public class UserService implements org.example.utils.observer.Observable<Duck> {
    static long maxId = -1;
    private final Repository<Long, Person> personRepo;
    private final DuckRepo duckRepo;
    
    // Lista de observeri
    private List<org.example.utils.observer.Observer<Duck>> observers = new ArrayList<>();

    /**
     * Creeaza un nou service cu repository-urile furnizate.
     *
     * @param personRepo repository pentru persoane
     * @param duckRepo repository pentru rate
     */
    public UserService(Repository<Long, Person> personRepo, DuckRepo duckRepo) {
        this.personRepo = personRepo;
        this.duckRepo = duckRepo;
    }
    
    @Override
    public void addObserver(org.example.utils.observer.Observer<Duck> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(org.example.utils.observer.Observer<Duck> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(Duck t) {
        observers.forEach(o -> o.update(t));
    }

    /**
     * Creeaza si salveaza o persoana cu un ID nou generat.
     *
     * @param person entitatea persoana (fara ID setat)
     * @return persoana salvata
     * @throws RuntimeException daca salvarea esueaza in repository
     */// Add import

// ...

    /**
     * Creeaza si salveaza o persoana cu un ID nou generat.
     *
     * @param person entitatea persoana (fara ID setat)
     * @return persoana salvata
     * @throws RuntimeException daca salvarea esueaza in repository
     */
    public Person createPerson(Person person) {
        person.setID(generateNextId());
        // Hash password before saving
        person.setPassword(PasswordHasher.hash(person.getPassword()));
        return personRepo.save(person);
    }

    /**
     * Creeaza si salveaza o rata cu un ID nou generat.
     *
     * @param duck entitatea rata (fara ID setat)
     * @return rata salvata
     * @throws RuntimeException daca salvarea esueaza in repository
     */
    public Duck createDuck(Duck duck) {
        duck.setID(generateNextId());
        // Hash password before saving
        duck.setPassword(PasswordHasher.hash(duck.getPassword()));
        Duck saved = duckRepo.save(duck);
        notifyObservers(saved); // Notificăm observerii că s-a creat o rață
        return saved;
    }


    /**
     * Genereaza urmatorul ID global pe baza maximului din repo-uri la prima invocare,
     * apoi incrementeaza secvential.
     *
     * @return ID nou unic
     */
    private long  generateNextId(){
        if(maxId == -1){
            for(Duck d: duckRepo.findAll()){
                if(d.getID() != null && d.getID() > maxId){
                    maxId = d.getID();
                }
            }
            for (Person p : personRepo.findAll()) {
                if (p.getID() != null && p.getID() > maxId) {
                    maxId = p.getID();
                }
            }

        }
        maxId = maxId + 1;
        return maxId;
    }

    /**
     * Returneaza toate entitatile de tip Duck.
     *
     * @return iterator peste toate ratele
     */
    public Iterable<Duck> getAllDucks(){
        return duckRepo.findAll();
    }
    /**
     * Returneaza toate entitatile de tip Person.
     *
     * @return iterator peste toate persoanele
     */
    public Iterable<Person> getAllPeople(){
        return personRepo.findAll();
    }

    /**
     * Autentifica un utilizator dupa username si parola.
     * Cauta mai intai in Duck, apoi in Person.
     *
     * @param username numele de utilizator
     * @param password parola in clar
     * @return utilizatorul autentificat
     * @throws RuntimeException daca parola este gresita sau utilizatorul nu exista
     */
    public User login(String username, String password) {
        User u = getDuckByUsername(username);
        if (u != null) {
            if (PasswordHasher.check(password, u.getPassword())) {
                return u;
            } else {
                throw new RuntimeException("Incorrect password!");
            }
        }
        u = getPersonByUsername(username);
        if (u != null) {
            if (PasswordHasher.check(password, u.getPassword())) {
                return u;
            } else {
                throw new RuntimeException("Incorrect password!");
            }
        }
        throw new RuntimeException("User does not exist!");
    }

    /**
     * Gaseste o persoana dupa username.
     *
     * @param username numele de utilizator
     * @return persoana gasita sau null daca nu exista
     */
    public Person getPersonByUsername(String username){
        for(Person p: personRepo.findAll()){
            if(Objects.equals(p.getUsername(), username)){
                return p;
            }
        }
        return null;
    }

    /**
     * Gaseste o rata dupa username.
     *
     * @param username numele de utilizator
     * @return rata gasita sau null daca nu exista
     */
    public Duck getDuckByUsername(String username){
        for(Duck d: duckRepo.findAll()){
            if(Objects.equals(d.getUsername(), username)){
                return d;
            }
        }
        return null;
    }

    /**
     * Returneaza ratele filtrate dupa tip.
     * Foloseste optimizarea din baza de date daca repository-ul permite.
     *
     * @param type tipul de rata
     * @return iterator cu ratele filtrate
     */
    public Iterable<Duck> filterDucksByType(DuckType type) {
        return duckRepo.findByType(type);

    }

    /**
     * Cauta utilizatori (persoane si rate) ale caror username contine substringul dat (case-insensitive).
     *
     * @param name substring de cautat in username
     * @return lista de utilizatori potriviti
     */
    public Iterable<User> searchUserName(String name) {
        List<User> list = new ArrayList<>();
        for (Person p : personRepo.findAll()) {
            if (p.getUsername().toLowerCase().contains(name.toLowerCase())) {
                list.add(p);
            }
        }
        for (Duck d : duckRepo.findAll()) {
            if (d.getUsername().toLowerCase().contains(name.toLowerCase())) {
                list.add(d);
            }
        }
        return list;
    }

    /**
     * Sterge utilizatorul cu ID dat si il elimina din listele de prieteni ale tuturor celorlalti utilizatori.
     *
     * @param id identificatorul utilizatorului
     * @throws RuntimeException daca nu exista utilizator cu acest ID
     */
    public void deleteUser(long id){
        User target = duckRepo.findOne(id);
        if (target == null) target = personRepo.findOne(id);
        if (target == null) throw new RuntimeException("Nu exista user cu acest id");


        duckRepo.delete(id);
        personRepo.delete(id);
        
        // Notificăm observerii că s-a șters ceva (trimitem null sau un obiect dummy, sau chiar target-ul șters)
        // Aici simplificăm și trimitem null pentru a semnala "schimbare generică" sau target-ul dacă e Duck
        if (target instanceof Duck) {
             notifyObservers((Duck) target);
        }
    }

    /**
     * Construieste o lista cu toti utilizatorii (persoane si rate).
     *
     * @return lista tuturor utilizatorilor
     */
    public List<User> getAllUsers(){
        List<User> allUser = new ArrayList<>();
        for(Person p: getAllPeople()) allUser.add(p);
        for(Duck d: getAllDucks()) allUser.add(d);
        return allUser;
    }


    public Page<Duck> getAllDucksPaged(Pageable pageable) {

        return ((PagingRepository<Long, Duck>) duckRepo).findAllOnPage(pageable);
    }

    public Page<Duck> getDucksByTypePaged(DuckType type, Pageable pageable) {
        return duckRepo.findAllOnPage(pageable, type);
    }
}
