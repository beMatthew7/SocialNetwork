package org.example.domain.validators;


import org.example.domain.Person;

public class PersonValidator extends BaseUserValidator implements Validator<Person> {

    @Override
    public void validate(Person person) throws ValidationException {
        String errors = getBaseUserErrors(person);

        if (person.getFirstName() == null || person.getFirstName().trim().isEmpty()) {
            errors += "Prenumele (firstName) nu poate fi gol!\n";
        }

        if (person.getSecondName() == null || person.getSecondName().trim().isEmpty()) {
            errors += "Numele (secondName) nu poate fi gol!\n";
        }

        if (person.getEmpathyNivel() < 0) {
            errors += "Nivelul de empatie nu poate fi negativ!\n";
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
