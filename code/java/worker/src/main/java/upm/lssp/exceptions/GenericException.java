package upm.lssp.exceptions;

public class GenericException extends Throwable {

    private final String message;

    public GenericException(String s) {
        this.message=s;
    }

    @Override
    public String getMessage() {
        return message;
    }
    @Override
    public String toString() {
        return "GenericException: " + super.getMessage();
    }
}
