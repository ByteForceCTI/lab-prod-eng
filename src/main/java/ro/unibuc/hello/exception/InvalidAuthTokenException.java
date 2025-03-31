package ro.unibuc.hello.exception;

public class InvalidAuthTokenException extends RuntimeException {
    private static final String invalidAuthTkExcTemplate = "Invalid Auth token: %s";

    public InvalidAuthTokenException(String message) {
        super(String.format(invalidAuthTkExcTemplate, message));
    }
}
