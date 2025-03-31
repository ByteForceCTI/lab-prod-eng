package ro.unibuc.hello.exception;

public class ForbiddenAccessException extends RuntimeException {

    private static final String forbiddenAccessTemplate = "Forbidden: %s";

    public ForbiddenAccessException(String message) {
        super(String.format(forbiddenAccessTemplate, message));
    }
}
