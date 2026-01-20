package org.example.domain.validators;

public class NoOpValidator<T> implements Validator<T> {
    @Override
    public void validate(T entity) throws ValidationException {
        // no validation
    }
}







