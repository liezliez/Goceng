package id.co.bcaf.goceng.exceptions;

public class LoanAmountExceededException extends RuntimeException {
    public LoanAmountExceededException() {
        super();
    }

    public LoanAmountExceededException(String message) {
        super(message);
    }
}
