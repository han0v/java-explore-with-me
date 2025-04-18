package ru.practicum.exception;

public class DateTimeValidationException extends RuntimeException {
    public DateTimeValidationException(String message) {
        super(message);
    }
}
