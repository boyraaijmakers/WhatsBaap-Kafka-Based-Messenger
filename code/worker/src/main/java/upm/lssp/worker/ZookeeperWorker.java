package upm.lssp.worker;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import upm.lssp.Status;
import upm.lssp.exceptions.*;
import upm.lssp.messages.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static upm.lssp.Config.*;

public class ZookeeperWorker {

    private ZooKeeper zoo = null;
    public KafkaWorker kafka = null;
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

                    try {
                        handleWatcher(we.getPath(), path.split("/")[2], null);
                    } catch (RequestException e) {
                        e.printStackTrace();
                    }
                }
            };


            String result = new String(zoo.getData("/request/" + action + "/" + username, w, null));

            TimeUnit.SECONDS.sleep(1);



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
        if (zoo == null) {
            try {
                connect();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }
        HashMap<Status, List<String>> users = new HashMap<>();

        List<String> onlineNodes = null;
        List<String> allNodes = null;
        try {
            onlineNodes = zoo.getChildren("/online", false);
            allNodes = zoo.getChildren("/registry", false);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        final List<String> online = onlineNodes == null ? new ArrayList<>() : onlineNodes;

        if (allNodes == null) allNodes = new ArrayList<>();


        List<String> offline = allNodes.stream().filter(u -> !online.contains(u)).collect(Collectors.toList());


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
    private void handleWatcher(String path, String action, String res) throws RequestException {
        String result;
        if (res == null) {
            try {
                result = new String(zoo.getData(path, false, null));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RequestException("Failure in getting the data from the node");
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
        if (result.equals("0")) {
            throw new RequestException("The manager has set the node to 0");
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
     * @throws GeneralException
     */
    public boolean register(String username) throws GeneralException {

        if (zoo == null) connect();
        if (!username.matches("^[a-zA-Z0-9]+")) {
            throw new RegistrationException("The username contains illegal characters. Please use only upper-lower case letters and check that its length is less than 25");
        } else if (username.length() > 25) {
            throw new RegistrationException("The username choosen is too long. Please use only upper-lower case letters and check that its length is less than 25");
        } else if (checkNode("/request/enroll/" + username) != null) {
            throw new RegistrationException(username + " already has a pending enrollment request! Choose a new one");
        } else if (checkNode("/registry/" + username) != null && checkNode("/online/" + username) == null) {
            if (DEBUG) System.out.println(username + " is already registered! View will pass him/her online");
            return true;
        } else if (checkNode("/online/" + username) != null) {
            throw new RegistrationException(username + " is already taken. Choose another one");
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
     * @throws GeneralException
     */
    public boolean quit(String username) throws GeneralException {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null) {
            throw new QuitException(username + " is not registered!");
        } else if (checkNode("/request/quit/" + username) != null) {
            registered = false;
            return true;
        }
        try {
            if (createRequest(username, "quit")) {
                goOffline();
            }
        } catch (RequestException e) {
            throw new QuitException(e.getMessage());
        }
        registered = false;
        return true;

    }

    /**
     * Make the user to go online and add a hook when the app closes to quit
     *
     * @param username
     * @return
     * @throws ConnectionException
     */
    public boolean goOnline(String username) throws GeneralException {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null) {
            throw new GeneralException(username + " is not registered");
        }


        this.kafka = new KafkaWorker(username);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (registered) this.quit(username);
            } catch (GeneralException e) {
                e.printStackTrace();
            }
        }));

        if (DEBUG) System.out.println("Kafka worker created for " + username);
        setStatusOnline(username);
        return true;

    }

    /**
     * Close the connection with the user. The node created in /online is EPHEMERAL, thus
     * it will be deleted automatically from zk /online path, but will still exists in /registry
     *
     * @return
     * @throws ConnectionException
     */
    public boolean goOffline() throws GeneralException {
        if (zoo == null) connect();

        kafka.shutdownConsumer();

        try {
            zoo.close();
        } catch (InterruptedException e) {
            throw new ConnectionException("Connection error while going offline");
        }
        zoo = null;
        return true;
    }

    /**
     * Method called to send a message
     * @param message
     * @return
     * @throws SendException
     */
    public boolean sendMessage(Message message) throws SendException {
        if (DEBUG) System.out.println("Send message request");
        if (zoo == null) {
            try {
                connect();
            } catch (ConnectionException e) {
                throw new SendException("ZooKeeper was not able to connect while trying to send a message");
            }
        }

        if (!isOnline(message.getReceiver())) {
            throw new SendToOfflineException(message.getReceiver() + " is offline. Try later");
        }

        kafka.producer(message);

        if (DEBUG) System.out.println("Message sent");
        return true;

    }

    private boolean isOnline(String user) {
        return checkNode("/online/" + user) != null;
    }

}
