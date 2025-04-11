package ru.practicum.exception;

public class StatsServiceException extends RuntimeException {
    public StatsServiceException(String message) {
        super(message);
    }
}
