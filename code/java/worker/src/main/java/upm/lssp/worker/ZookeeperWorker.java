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
    private boolean registered;

    public ZookeeperWorker() throws ConnectionException {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.FATAL);
        connect();
    }

    /**
     * Connects to ZK services
     *
     * @return
     * @throws ConnectionException
     */
    private String connect() throws ConnectionException {

        final CountDownLatch connectionLatch = new CountDownLatch(1);
        try {
            zoo = new ZooKeeper(ZKSERVER, ZKSESSIONTIME, we -> {
                if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectionLatch.countDown();
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
     *
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

            Watcher w = we -> {
                if (we.getType() == Watcher.Event.EventType.NodeDataChanged) {

                    handleWatcher(we.getPath(), path.split("/")[2], null);
                }
            };


            String result = new String(zoo.getData("/request/" + action + "/" + username, w, null));

            TimeUnit.SECONDS.sleep(2);

            if (checkNode("/request/" + action + "/" + username) == null) {
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

    /**
     * Return an HashMap with keys belonging to the Status enumeration, containing
     * a list of users
     *
     * @return
     */
    public HashMap<Status, List<String>> retrieveUserList() {
        HashMap<Status, List<String>> users = new HashMap<>();

        List<String> online = null;
        List<String> offline = null;
        try {
            online = zoo.getChildren("/online", false);
            offline = zoo.getChildren("/registry", false);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        if (offline != null) {
            offline.remove(online);
        }

        users.put(Status.ONLINE, online);
        users.put(Status.OFFLINE, offline);
        return users;
    }


    /**
     * Set the status of the user to online
     *
     * @param username
     */
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
     * Handles the tasks to be fulfilled by the worker after that the
     * manager has finished its job
     *
     * @param path
     * @param action
     * @param res
     */
    private void handleWatcher(String path, String action, String res) {
        String result;
        if (res == null) {
            try {
                result = new String(zoo.getData(path, false, null));
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
            this.registered = action.equals("enroll");
        }

        if (DEBUG) System.out.println("Handled request " + path + "!");
    }


    /**
     * Check if the node exists
     *
     * @param path
     * @return
     */
    public Stat checkNode(String path) {
        try {
            return zoo.exists(path, false);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Register a new user
     *
     * @param username
     * @return
     * @throws GenericException
     */
    public boolean register(String username) throws GenericException {

        if (zoo == null) connect();
        if (checkNode("/registry/" + username) != null) {
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

    /**
     * Quit the user
     *
     * @param username
     * @return
     * @throws GenericException
     */
    public boolean quit(String username) throws GenericException {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null) {
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

    /**
     * Make the user to go online
     *
     * @param username
     * @return
     * @throws ConnectionException
     */
    public boolean goOnline(String username) throws ConnectionException {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null) {
            return false;
        }

        setStatusOnline(username);
        return true;

    }

    /**
     * Close the connection with the user. The node created in /online is EPHEMERAL, thus
     * it will be deleted automatically from zk /online path, but will still exists in /registry
     *
     * @return
     * @throws ConnectionException
     * @throws InterruptedException
     */
    public boolean goOffline() throws ConnectionException, InterruptedException {
        if (zoo == null) connect();
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
