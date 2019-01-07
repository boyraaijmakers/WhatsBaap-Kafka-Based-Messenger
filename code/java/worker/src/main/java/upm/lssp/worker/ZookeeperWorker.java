package upm.lssp.worker;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import upm.lssp.Status;
import upm.lssp.exceptions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static upm.lssp.Config.*;

public class ZookeeperWorker {

    private ZooKeeper zoo = null;
    private Boolean registered = false;
    private Boolean online = false;

    public ZookeeperWorker() throws ConnectionException {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.FATAL);
        connect();
    }

    private String connect() throws ConnectionException {

        final CountDownLatch connectionLatch = new CountDownLatch(1);
        try {
            zoo=new ZooKeeper(ZKSERVER, ZKSESSIONTIME, new Watcher() {
                public void process(WatchedEvent we) {
                    if (we.getState() == Event.KeeperState.SyncConnected) {
                        connectionLatch.countDown();
                    }
                }
            });
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage());
        }

        try {
            connectionLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new ConnectionException(e.getMessage());
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

        if (DEBUG) {
            System.out.println("Request: " + path);
        }

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


            String result = new String(zoo.getData("/request/" + action + "/" + username, w, null));

            TimeUnit.SECONDS.sleep(2);

            if (checkNode("/request/" + action + "/" + username) == null) {
                System.out.println("R: " + result);
                return true;
            } else if (!result.equals(("-1"))) {
                handleWatcher(path, action, result);
                return true;
            }


            throw new RequestException("The manager didn't handle the request. Check that the manager is working and try again");


        } catch (Exception e) {
            throw new RequestException(e.getMessage());
        }
    }

    public HashMap<Status, List<String>> retrieveUserList() {
        HashMap<Status, List<String>> users = new HashMap<>();

        List<String> online = null;
        List<String> offline = null;
        try {
            online = zoo.getChildren("/online", false);
            offline = zoo.getChildren("/registry", false);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        offline.remove(online);

        users.put(Status.ONLINE, online);
        users.put(Status.OFFLINE, offline);
        return users;
    }


    private void setStatusOnline(String username) {
        final String path = "/online/" + username;

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

        System.out.println("Handled request " + path + "!");
    }


    public Stat checkNode(String path){
        try {
            return zoo.exists(path, false);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean register(String username) throws GenericException {

        if (zoo == null) connect();
        if (checkNode("/registry/" + username) != null ) {
            if (DEBUG) System.out.printf(username + " is already registered! Passing to online");
            goOnline(username);
            return true;
        } else if (checkNode("/request/enroll/" + username) != null) {
            throw new RegistrationException(username + " already has a pending enrollment request! Choose a new one");
        }

        try {
            return createRequest(username, "enroll");
        } catch (RequestException e) {
            throw new RegistrationException(e.getMessage());
        }

    }

    public boolean quit(String username) throws GenericException {
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

    public boolean goOnline(String username) throws ConnectionException {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null ) {
            return false;
        }

        setStatusOnline(username);
        return true;

    }

    public boolean goOffline(String username) throws ConnectionException, InterruptedException {
        if (zoo == null) connect();
        online = false;
        zoo.close();
        zoo = null;
        return false;
    }

    public void sendMessage() {
        //if (zoo == null) connect();

    }

    public void readMessages() {
        //if (zoo == null) connect();
    }

    public String getOnlineUsers(String username) {


        try {
            if (zoo == null) connect();
            if (checkNode("/online/" + username) != null) {
                return zoo.getChildren("/online", false).toString();
            } else {
                return "Only online users see other online users";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
