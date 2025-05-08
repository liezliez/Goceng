package id.co.bcaf.goceng.exceptions;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException() {
        super("Application not found");
    }

    public ApplicationNotFoundException(String message) {
        super(message);
    }
}
