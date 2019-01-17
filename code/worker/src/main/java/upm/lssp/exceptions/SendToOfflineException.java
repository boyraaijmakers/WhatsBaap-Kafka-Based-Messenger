package upm.lssp.exceptions;

public class SendToOfflineException extends SendException {


    /**
     * Exception thrown when the offline procedure fails
     * @param s message
     */
    public SendToOfflineException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "SendToOfflineException: " + super.getMessage();
    }


}
