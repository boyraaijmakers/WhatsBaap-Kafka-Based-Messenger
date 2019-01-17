package upm.lssp.exceptions;

public class RequestException extends GenericException {


    public RequestException(String s) {
        super(s);
    }

    @Override
    public String toString() {
        return "RequestException: " + super.getMessage();
    }


}
