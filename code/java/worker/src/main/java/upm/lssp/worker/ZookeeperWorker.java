package upm.lssp.worker;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.NodeChangeEvent;

public class ZookeeperWorker {

    ZooKeeper zoo = null;

    public ZookeeperWorker() {
        connect();
    }

    private String connect() {
        try {
            final CountDownLatch connectionLatch = new CountDownLatch(1);
            zoo = new ZooKeeper("localhost:2181", 1000, new Watcher() {
                public void process(WatchedEvent we) {
                    if (we.getState() == Event.KeeperState.SyncConnected) {
                        connectionLatch.countDown();
                    }
                }
            });

            connectionLatch.await(10, TimeUnit.SECONDS);
            ZooKeeper.States state = zoo.getState();
            System.out.println(state.toString());
            return state.toString();

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String createRequest(String username, String a) {
        final String action = a;
        try {
            zoo.create(
                    "/request/" + action + "/" + username,
                    "-1".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);


            return "Success!";

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private synchronized void handleWatcher(WatchedEvent we, String action) {
        System.out.println(we.getPath());
        System.out.println(we.getState());
        System.out.println(we.getType());
    }

    private Stat checkNode(String path){
        try {
            return zoo.exists(path, false);
        } catch (Exception e) {
            return null;
        }
    }

    public String register(String username) {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) != null ) {
            return username + " is already registered!";
        } else if (checkNode("/request/enroll/" + username) != null) {
            return username + " already has a pending enrollment request!";
        } else {
            return createRequest(username, "enroll");
        }
    }

    public String quit(String username) {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null ) {
            return username + " is not registered!";
        } else if (checkNode("/request/quit/" + username) != null) {
            return username + " already has a pending quit request!";
        } else {
            createRequest(username, "quit");
        }

        return null;
    }

    public boolean goOnline(String username) {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) != null) {
            return true;
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

    public void sendMessage() {
        if (zoo == null) connect();

    }

    public void readMessages() {
        if (zoo == null) connect();

    }

    public String getOnlineUsers(String username) {
        if (zoo == null) connect();

        try {
            if (checkNode("/online/" + username) != null) {
                zoo.getChildren("/online", false);
            } else {
                return "Only logged-in users see other online users";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
