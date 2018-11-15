package upm.lssp.worker;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZookeeperWorker {

    ZooKeeper zoo = null;
    Boolean registered = false;
    Boolean online = false;

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

    /**
     * Create a node with the username and append it to request
     * @param username
     * @param action
     * @return
     */
    private String createRequest(String username, String action) {

        final String path = "/request/" + action + "/" + username;

        try {
            zoo.create(
                    path,
                    "-1".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);

            Watcher w = new Watcher() {
                public void process(WatchedEvent we) {
                    if(we.getType() == Event.EventType.NodeDataChanged) {
                        try {
                            handleWatcher(we.getPath(), path.split("/")[2], null);
                        } catch (Exception e) {

                        }
                    }
                }
            };

            byte[] data =  zoo.getData("/request/" + action + "/" + username,
                    w, null);
            String result = new String(data);

            if(result != "-1") {
                handleWatcher(path, action, result);
            }

            return "Success!";

        } catch (Exception e) {
            System.out.println("Error");
            return e.getMessage();
        }
    }

    private void setStatus(String username, String status) {
        final String path = "/"+status+"/" + username;

        try {
            zoo.create(
                    path,
                    null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param path
     * @param action
     * @param res
     */
    private void handleWatcher(String path, String action, String res) {
        String result;

        if(res == null) {
            try {
                result =  new String(zoo.getData(path, false, null));
            } catch (Exception e) {
                e.printStackTrace();
                result = "0";
            }
        } else {
            result = res;
        }

        try {
            zoo.delete(path, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result.equals("1") || result.equals("2")) {
            if(action.equals("enroll")){
                this.registered = true;
            } else {
                this.registered = false;
            }


            }


        }
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

        return "Deletion success";
    }

    public boolean goOnline(String username) {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null ) {
            return false;
        }

        setStatus(username,"online");
        return true;

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
                return "Only online users see other online users";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
