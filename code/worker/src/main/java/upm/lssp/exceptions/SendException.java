package upm.lssp.exceptions;

public class SendException extends GeneralException {


    /**
     * Exception thrown when the sending process of a
     * message fails
     *
     * @param s error message
     */
    public SendException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "SendException: " + super.getMessage();
    }


}
