package org.example.repository;

import org.example.domain.Entity;
import org.example.domain.validators.Validator;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Repository abstract bazat pe fisier, care incarca datele in memorie si le persista la operatiile CRUD.
 * @param <ID> tipul identificatorului
 * @param <E> tipul entitatii
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {

    protected String fileName;

    /**
     * Creeaza repository-ul si incarca datele din fisier.
     * @param fileName calea fisierului
     * @param validator validatorul entitatii
     */
    public AbstractFileRepository(String fileName, Validator<E> validator) {
        super(validator);
        this.fileName = fileName;
        loadData();
    }

    /**
     * Incarca toate inregistrarile din fisier in memoria interna.
     */
    private void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String newLine;
            while ((newLine = reader.readLine()) != null) {
                if (newLine.trim().isEmpty()) {
                    continue;
                }

                List<String> data = Arrays.asList(newLine.split(";"));
                E entity = extractEntity(data);

                ID entityId = (ID) entity.getID();
                super.entities.put(entityId, entity);
            }
        } catch (FileNotFoundException e) {
            // Fisierul va fi creat la primul save
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Construieste o entitate din lista de atribute citite din fisier.
     * @param attributes lista de atribute
     * @return entitatea construita
     */
    public abstract E extractEntity(List<String> attributes);

    /**
     * Construieste reprezentarea text a entitatii pentru persistare in fisier.
     * @param entity entitatea
     * @return linia text scrisa in fisier
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Scrie in fisier o singura entitate (append).
     * @param entity entitatea de scris
     */
    protected void writeToFile(E entity) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(createEntityAsString(entity));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rescrie intregul fisier cu toate entitatile curente din memorie.
     */
    protected void writeAllToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            for (E entity : findAll()) {
                writer.write(createEntityAsString(entity));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    /**
     * Salveaza entitatea si o persista in fisier daca operatia reuseste.
     */
    public E save(E entity) {
        E result = super.save(entity);
        if (result == null) {
            writeToFile(entity);
        }
        return result;
    }

    @Override
    /**
     * Sterge entitatea si rescrie fisierul daca operatia reuseste.
     */
    public E delete(ID id) {
        E result = super.delete(id);
        if (result != null) {
            writeAllToFile();
        }
        return result;
    }

    @Override
    /**
     * Actualizeaza entitatea si rescrie fisierul daca operatia reuseste.
     */
    public E update(E entity) {
        E result = super.update(entity);
        if (result == null) {
            writeAllToFile();
        }
        return result;
    }
}