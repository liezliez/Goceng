package id.co.bcaf.goceng.exceptions;

public class IncompleteCustomerDataException extends RuntimeException {
    public IncompleteCustomerDataException(String message) {
        super(message);
    }
}
