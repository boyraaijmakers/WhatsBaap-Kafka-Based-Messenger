package upm.lssp.exceptions;

public class ConnectionException extends GeneralException {


    /**
     * Connection exception is thrown when the system
     * cannot connect to ZooKeeper or Kafka
     * @param s error string
     */
    public ConnectionException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "ConnectionException: " + super.getMessage();
    }


}
