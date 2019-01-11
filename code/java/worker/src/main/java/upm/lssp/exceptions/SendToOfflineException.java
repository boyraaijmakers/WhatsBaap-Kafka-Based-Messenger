package upm.lssp.exceptions;

public class SendToOfflineException extends SendException {


    public SendToOfflineException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "SendToOfflineException: " + super.getMessage();
    }


}
