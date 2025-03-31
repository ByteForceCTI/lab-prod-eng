package ro.unibuc.hello.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // map EntityNotFoundException to HTTP 404 code 
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        logger.error("Entity not found: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // map UnauthorizedException to HTTP 401 code 
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        logger.error("Unauthorized action: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

     // map UnauthorizedException to HTTP 403 code 
     @ExceptionHandler(ForbiddenAccessException.class)
     public ResponseEntity<String> handleForbiddenAccessException(ForbiddenAccessException ex, WebRequest request) {
         logger.error("Forbidden action: {} - Request: {}", ex.getMessage(), request.getDescription(false));
         return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
     }

     // map MissingRequestHeaderException to HTTP 401 code
     @ExceptionHandler(MissingRequestHeaderException.class)
     public ResponseEntity<String> handleMissingRequestHeaderException(MissingRequestHeaderException ex, WebRequest request) {
        logger.error("Missing header: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // map InvalidAuthTokenException to HTTP 401 code
    @ExceptionHandler(InvalidAuthTokenException.class)
    public ResponseEntity<String> handleInvalidAuthTokenException(InvalidAuthTokenException ex, WebRequest request) {
       logger.error("Invalid auth token: {} - Request: {}", ex.getMessage(), request.getDescription(false));
       return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
   }

    // map any untreated exception to error 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("Unhandled exception occurred while processing request: {}",
                     request.getDescription(false), ex);
        // custom error message for generic exception
        return new ResponseEntity<>("An internal error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
