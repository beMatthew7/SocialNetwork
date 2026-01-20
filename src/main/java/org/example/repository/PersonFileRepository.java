package org.example.repository;

import org.example.domain.Person;
import org.example.domain.validators.Validator;

import java.util.Date;
import java.util.List;

/**
 * Repository pentru entitati Person, persistat in fisier.
 */
public class PersonFileRepository extends AbstractFileRepository<Long, Person> {

    /**
     * Creeaza repository-ul pentru Person.
     * @param fileName calea fisierului
     * @param validator validatorul entitatii Person
     */
    public PersonFileRepository(String fileName, Validator<Person> validator) {
        super(fileName, validator);
    }


    @Override
    /**
     * Extrage un Person din lista de atribute citite din fisier.
     */
    public Person extractEntity(List<String> attributes) {
        Long id = Long.parseLong(attributes.get(0));
        String username = attributes.get(1);
        String email = attributes.get(2);
        String password = attributes.get(3);
        String firstName = attributes.get(4);
        String secondName = attributes.get(5);

        Date dateOfBirth = new Date(Long.parseLong(attributes.get(6)));
        String occupation = attributes.get(7);
        int empathyNivel = Integer.parseInt(attributes.get(8));

        Person person = new Person(username, email, password, firstName,
                secondName, dateOfBirth, occupation, empathyNivel);
        person.setID(id);
        return person;
    }


    @Override
    /**
     * Creeaza reprezentarea text a unui Person pentru scriere in fisier.
     */
    protected String createEntityAsString(Person entity) {
        return entity.getID() + ";" +
                entity.getUsername() + ";" +
                entity.getEmail() + ";" +
                entity.getPassword() + ";" +
                entity.getFirstName() + ";" +
                entity.getSecondName() + ";" +
                entity.getDateOfBirth().getTime() + ";" +
                entity.getOccupation() + ";" +
                entity.getEmpathyNivel();
    }
}