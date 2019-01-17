package upm.lssp.exceptions;

public class GeneralException extends Exception {

    private final String message;

    /**
     * General Exception is the highest exception in
     * the hierarchy
     *
     * @param s error message
     */
    public GeneralException(String s) {
        this.message=s;
    }

    @Override
    public String getMessage() {
        return message;
    }
    @Override
    public String toString() {
        return "GeneralException: " + super.getMessage();
    }
}
