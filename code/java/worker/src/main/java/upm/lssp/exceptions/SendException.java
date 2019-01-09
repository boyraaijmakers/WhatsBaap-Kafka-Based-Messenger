package upm.lssp.exceptions;

public class SendException extends GenericException {


    public SendException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "SendException: " + super.getMessage();
    }


}
