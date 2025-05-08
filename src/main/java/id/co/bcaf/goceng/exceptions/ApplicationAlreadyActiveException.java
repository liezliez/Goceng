package id.co.bcaf.goceng.exceptions;

public class ApplicationAlreadyActiveException extends RuntimeException {
    public ApplicationAlreadyActiveException() {
        super("Application is already active");
    }

    public ApplicationAlreadyActiveException(String message) {
        super(message);
    }
}
