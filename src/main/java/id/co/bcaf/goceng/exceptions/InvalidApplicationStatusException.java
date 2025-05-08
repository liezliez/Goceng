package id.co.bcaf.goceng.exceptions;

public class InvalidApplicationStatusException extends RuntimeException {
    public InvalidApplicationStatusException() {
        super("Invalid application status");
    }

    public InvalidApplicationStatusException(String message) {
        super(message);
    }
}
