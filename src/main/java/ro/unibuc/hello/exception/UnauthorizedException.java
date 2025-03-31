package ro.unibuc.hello.exception;

public class UnauthorizedException extends RuntimeException {

    private static final String unauthorizedExcTemplate = "Unauthorized: %s";

    public UnauthorizedException(String message) {
        super(String.format(unauthorizedExcTemplate, message));
    }
}
