package org.example.domain.validators;

import org.example.domain.Duck;

public class DuckValidator extends BaseUserValidator implements Validator<Duck> {

    @Override
    public void validate(Duck duck) throws ValidationException {

        String errors = getBaseUserErrors(duck);


        if (duck.getSpeed() <= 0) {
            errors += "Viteza (speed) trebuie sa fie pozitiva!\n";
        }

        if (duck.getEndurance() <= 0) {
            errors += "Rezistenta (endurance) trebuie sa fie pozitiva!\n";
        }


        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}