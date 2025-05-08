package id.co.bcaf.goceng.exceptions;

public class UnauthorizedBranchException extends RuntimeException {
    public UnauthorizedBranchException() {
        super("Unauthorized branch access");
    }

    public UnauthorizedBranchException(String message) {
        super(message);
    }
}
