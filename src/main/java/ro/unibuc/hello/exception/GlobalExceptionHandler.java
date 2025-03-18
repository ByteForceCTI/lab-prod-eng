package ro.unibuc.hello.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // This method catches all exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("Unhandled exception occurred while processing request: {}",
                     request.getDescription(false), ex);
        // Customize the error response as needed.
        return new ResponseEntity<>("An internal error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

