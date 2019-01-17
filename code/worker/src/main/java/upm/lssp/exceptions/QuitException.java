package upm.lssp.exceptions;

public class QuitException extends GeneralException {


    /**
     * Exception thrown when the quitting
     * process doesn't go well
     * @param s error message
     */
    public QuitException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "QuitException: " + super.getMessage();
    }


}
