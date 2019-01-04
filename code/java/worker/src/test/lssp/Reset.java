package lssp;

public class Reset {
    public static void main(String[] args) {
        TestTools.connect();
        TestTools.initializeEnv();
        TestTools.closeConnection();
    }
}
