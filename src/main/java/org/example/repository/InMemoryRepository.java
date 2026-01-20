package org.example.repository;

import org.example.domain.Entity;
import org.example.domain.validators.Validator;
import org.example.domain.validators.ValidationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementarea de baza a Repository care stocheaza datele in memorie.
 *
 * @param <ID> Tipul ID-ului (care in practica va fi Long)
 * @param <E>  Tipul entitatii (ex: Person, Duck)
 */

public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {


    protected Map<ID, E> entities;
    protected Validator<E> validator;

    /**
     * Creeaza un repository in memorie cu validatorul specificat.
     *
     * @param validator validatorul entitatilor
     */
    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
        this.entities = new HashMap<>();
    }

    @Override
    /**
     * Gaseste o entitate dupa ID.
     * @param id ID-ul entitatii, nu poate fi null
     * @return entitatea sau null daca nu exista
     * @throws IllegalArgumentException daca id este null
     */
    public E findOne(ID id) {
        if (id == null)
            throw new IllegalArgumentException("Id-ul nu poate fi null!");

        return entities.get(id);
    }

    @Override
    /**
     * Returneaza toate entitatile stocate.
     * @return iterabil peste toate entitatile
     */
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    /**
     * Salveaza o entitate daca ID-ul nu exista deja si entitatea este valida.
     * @param entity entitatea de salvat
     * @return null daca salvarea a reusit; altfel entitatea (daca ID-ul exista deja)
     * @throws ValidationException daca entitatea nu este valida
     * @throws IllegalArgumentException daca entitatea este null
     */
    public E save(E entity) throws ValidationException {
        if (entity == null)
            throw new IllegalArgumentException("Entitatea nu poate fi null!");


        validator.validate(entity);


        ID entityId = (ID) entity.getID();


        if (entities.containsKey(entityId)) {

            return entity;
        }


        entities.put(entityId, entity);


        return null;
    }

    @Override

    /**
     * Sterge entitatea cu ID-ul dat.
     * @param id ID-ul entitatii, nu poate fi null
     * @return entitatea eliminata sau null daca nu exista
     * @throws IllegalArgumentException daca id este null
     */
    public E delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id-ul nu poate fi null!");
        }

        return entities.remove(id);
    }

    @Override
    /**
     * Actualizeaza o entitate existenta.
     * @param entity entitatea de actualizat
     * @return null daca actualizarea a reusit; altfel entitatea (daca ID-ul nu exista)
     * @throws IllegalArgumentException daca entitatea este null
     * @throws ValidationException daca entitatea nu este valida
     */
    public E update(E entity) throws ValidationException {
        if (entity == null)
            throw new IllegalArgumentException("Entitatea nu poate fi null!");


        validator.validate(entity);


        ID entityId = (ID) entity.getID();


        if (!entities.containsKey(entityId)) {

            return entity;
        }


        entities.put(entityId, entity);


        return null;
    }
}