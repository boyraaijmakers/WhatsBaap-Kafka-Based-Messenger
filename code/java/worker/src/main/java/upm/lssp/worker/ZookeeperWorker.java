package upm.lssp.worker;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import upm.lssp.exceptions.ConnectionException;
import upm.lssp.exceptions.QuitException;
import upm.lssp.exceptions.RegistrationException;
import upm.lssp.exceptions.RequestException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static upm.lssp.Config.*;

public class ZookeeperWorker {

    private ZooKeeper zoo = null;
    private Boolean registered = false;
    private Boolean online = false;

    public ZookeeperWorker() {
        connect();
    }

    private String connect() {

            final CountDownLatch connectionLatch = new CountDownLatch(1);
        try {
            new ZooKeeper(ZKSERVER, ZKSESSIONTIME, new Watcher() {
                public void process(WatchedEvent we) {
                    if (we.getState() == Event.KeeperState.SyncConnected) {
                        connectionLatch.countDown();
                    }
                }
            });
        } catch (IOException e) {
            return(e.getMessage());
        }

        try {
            connectionLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return(e.getMessage());
        }

        ZooKeeper.States state = zoo.getState();
        return state.toString();

    }

    /**
     * Create a node with the username and append it to request
     * @param username
     * @param action
     * @return
     */
    private boolean createRequest(String username, String action) throws RequestException {

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

                            handleWatcher(we.getPath(), path.split("/")[2], null);
                    }
                }
            };

            byte[] data =  zoo.getData("/request/" + action + "/" + username,
                    w, null);
            String result = new String(data);

            if(result != "-1") {
                handleWatcher(path, action, result);
            }

            return true;

        } catch (Exception e) {
            throw new RequestException(e.getMessage());
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


    private Stat checkNode(String path){
        try {
            return zoo.exists(path, false);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean register(String username) throws RegistrationException {

        if (zoo == null) connect();

        if (checkNode("/registry/" + username) != null ) {
            throw new RegistrationException(username + " is already registered!");
        } else if (checkNode("/request/enroll/" + username) != null) {
            throw new RegistrationException(username + " already has a pending enrollment request!");
        }
        try {
            return createRequest(username, "enroll");
        } catch (RequestException e) {
            throw new RegistrationException(e.getMessage());
        }

    }

    public boolean quit(String username) throws QuitException {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null ) {
            throw new QuitException(username + " is not registered!");
        } else if (checkNode("/request/quit/" + username) != null) {
            throw new QuitException(username + " already has a pending quit request!");
        }
        try {
            return createRequest(username, "quit");
        } catch (RequestException e) {
            throw new QuitException(e.getMessage());
        }


    }

    public boolean goOnline(String username) {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null ) {
            return false;
        }

        setStatus(username,"online");
        online=true;
        return true;

    }

    public void goOffline() {
        if (zoo == null) connect();

        try {
            online=false;
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
