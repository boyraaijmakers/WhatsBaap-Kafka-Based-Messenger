package upm.lssp.exceptions;

public class RequestException extends GeneralException {


    /**
     * Exception thrown when a request to the
     * manager is not fulfilled
     *
     * @param s error String
     */
    public RequestException(String s) {
        super(s);
    }

    @Override
    public String toString() {
        return "RequestException: " + super.getMessage();
    }


}
