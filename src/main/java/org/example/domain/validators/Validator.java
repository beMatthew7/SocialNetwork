package org.example.domain.validators;

public interface Validator<T> {
    void validate(T user) throws ValidationException;
}
