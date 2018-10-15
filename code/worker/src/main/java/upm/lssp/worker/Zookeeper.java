package upm.lssp.worker;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Zookeeper {

    ZooKeeper zoo = null;

    private String connect() {
        try {
            final CountDownLatch connectionLatch = new CountDownLatch(1);
            zoo = new ZooKeeper("localhost:2181", 1000, new Watcher() {
                public void process(WatchedEvent we) {
                    if (we.getState() == Event.KeeperState.SyncConnected) {
                        connectionLatch.countDown();
                    }
                    if (we.getType() == Event.EventType.NodeCreated) {

                    }
                }
            });

            connectionLatch.await(10, TimeUnit.SECONDS);
            ZooKeeper.States state = zoo.getState();
            return state.toString();

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private void createRequest(String username, String a) {
        final String action = a;
        try {
            String created = zoo.create("/request/" + action + "/" + username, "-1".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            zoo.getData("/request/enroll/" + username, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    handleWatcher(watchedEvent, action);
                }
            }, null);
        } catch (Exception e) {

        }
    }

    private void handleWatcher(WatchedEvent we, String action) {

    }

    public String register(String username) {
        if (zoo == null) connect();

        createRequest(username, "enroll");

        return null;
    }

    public boolean goOnline(String username) {
        if (zoo == null) connect();

        try {
            Stat stat = zoo.exists("/registry/" + username, false);

            if (stat != null) {

            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public void goOffline() {
        if (zoo == null) connect();

        try {
            zoo.close();
        } catch (InterruptedException e) {

        }
    }

    public void quit(String username) {
        if (zoo == null) connect();

        createRequest(username, "quit");
    }

    public void sendMessage() {
        if (zoo == null) connect();

    }

    public void readMessages() {
        if (zoo == null) connect();

    }

    public void getOnlineUsers() {
        if (zoo == null) connect();

        try {
            Stat stat = zoo.exists("/online", true);

            if (stat != null) {
                zoo.getChildren("/online", false);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
