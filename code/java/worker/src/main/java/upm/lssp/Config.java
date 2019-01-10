package upm.lssp;

public class Config {
    /**
     * ZooKeeper Server address:port
     */
    public static final String ZKSERVER = "localhost:2181";
    /**
     * ZooKeeper Server Session Time
     */
    public static final int ZKSESSIONTIME = 1000;
    /**
     * Kafka Broker address:port
     */
    public static final String KAFKABROKER = "localhost:9092";
    /**
     * When debug is enabled all Systems out will be shown in the console
     */
    public static final boolean DEBUG = true;
    /**
     * Autologin of a test user can be useful in debug to skip the login screen
     */
    public static final boolean AUTOLOGIN = true;

}
