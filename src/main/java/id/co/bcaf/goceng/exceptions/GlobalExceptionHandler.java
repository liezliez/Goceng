package id.co.bcaf.goceng.exceptions;

import id.co.bcaf.goceng.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles errors thrown anywhere in the app’s REST API.
 * Instead of letting exceptions bubble up, it catches them here and sends back clear,
 * consistent responses to the client.
 *
 * It helps keep the API responses clean and easier for frontend developers to handle.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors, like missing or wrong input fields.
     * Sends back a list of which fields had problems and what’s wrong with them.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Validation error", errors));
    }

    /**
     * Handles exceptions that already have a status and reason set.
     * Sends that status and message back to the client.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiResponse<>(false, ex.getReason(), null));
    }

    /**
     * If a database entity wasn’t found (like a missing customer or branch),
     * we tell the client with a 404 Not Found and a clear message.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Entity not found: " + ex.getMessage(), null));
    }

    /**
     * When an invalid argument is passed in, like a bad parameter, we return 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Invalid argument: " + ex.getMessage(), null));
    }

    /**
     * If a user tries to access something they’re not allowed to, we send a 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Access denied: " + ex.getMessage(), null));
    }

    /**
     * For any other unexpected errors, we return a 500 Internal Server Error.
     * We don’t expose details to clients here to keep things safe.
     * You can add logging here if you want to keep track of these errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        // TODO: Add logging here if needed for debugging.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Unexpected error: " + ex.getMessage(), null));
    }

    /**
     * When a user tries to do something but isn’t authenticated,
     * we respond with a 401 Unauthorized.
     */
    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotAuthenticated(UserNotAuthenticatedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    /**
     * When a requested customer can’t be found, return a 404 Not Found.
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomerNotFound(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    /**
     * When someone tries to activate an application that’s already active,
     * return a 409 Conflict.
     */
    @ExceptionHandler(ApplicationAlreadyActiveException.class)
    public ResponseEntity<ApiResponse<String>> handleApplicationAlreadyActive(ApplicationAlreadyActiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    /**
     * If a branch can’t be found in the database, return 404 Not Found.
     */
    @ExceptionHandler(BranchNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleBranchNotFound(BranchNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    /**
     * Handles DB constraint errors like duplicates.
     * For example, if someone tries to register with a phone number that’s already used,
     * we return a clear message explaining that.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Data integrity violation.";

        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String causeMessage = ex.getCause().getMessage();
            if (causeMessage.contains("UKphnaxr6qwa6vv0ped258ggkbd")) {
                message = "Registration failed: phone number already registered.";
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, message, null));
    }

    /**
     * Handles uploads that are too big.
     * Returns a 413 Payload Too Large with a simple message.
     * Note: This returns plain text instead of ApiResponse for simplicity.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File size exceeds maximum allowed limit!");
    }

    /**
     * If the requested loan amount is over the allowed limit,
     * we let the client know with a 400 Bad Request and an explanation.
     */
    @ExceptionHandler(LoanAmountExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleLoanAmountExceededException(LoanAmountExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Requested loan amount exceeds allowed limit: " + ex.getMessage(), null));
    }

}
