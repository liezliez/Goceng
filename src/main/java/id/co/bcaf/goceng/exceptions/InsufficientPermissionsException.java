package id.co.bcaf.goceng.exceptions;

public class InsufficientPermissionsException extends RuntimeException {
    public InsufficientPermissionsException() {
        super("You do not have permission to perform this action");
    }

    public InsufficientPermissionsException(String message) {
        super(message);
    }
}
