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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static upm.lssp.Config.*;

public class ZookeeperWorker {

    private ZooKeeper zoo = null;
    private KafkaWorker kafka = null;
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
        if (zoo == null) {
            try {
                connect();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }
        HashMap<Status, List<String>> users = new HashMap<>();

        List<String> online = null;
        List<String> offline = null;
        try {
            online = zoo.getChildren("/online", false);
            offline = zoo.getChildren("/registry", false);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        if (online == null) {
            online = new ArrayList<>();
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
    public boolean goOnline(String username) throws GenericException {
        if (zoo == null) connect();

        if (checkNode("/registry/" + username) == null) {
            return false;
        }

        this.kafka = new KafkaWorker(username);

        try {
            createTopicRegistry(username);
        } catch (KeeperException | InterruptedException e) {
            throw new GenericException("ZooKeeper was not able to create the topic registry: " + e);
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
     */
    public boolean goOffline() throws GenericException {
        if (zoo == null) connect();
        try {
            zoo.close();
        } catch (InterruptedException e) {
            throw new ConnectionException("Connection error while going offline");
        }
        zoo = null;
        return false;
    }

    public boolean sendMessage(Message message) throws SendException {
        if (zoo == null) {
            try {
                connect();
            } catch (ConnectionException e) {
                throw new SendException("ZooKeeper was not able to connect while trying to send a message");
            }
        }


        //Create the topic registry if it's a new conversation
        try {
            createTopicRegistryConversation(message.getSender(), message.getReceiver());
        } catch (KeeperException | InterruptedException e) {
            throw new SendException("ZooKeeper was not able to create the topic registry conversation: " + e);
        }


        //Send the message
        kafka.sendMessage(message);

        //Notify the topic registry of the receiver so the consumer can wake up and consume the message
        try {
            zoo.setData("/topics/" + message.getReceiver() + "/" + message.getSender(), "1".getBytes(), 1);
        } catch (KeeperException | InterruptedException e) {
            throw new SendException("ZooKeeper was not bale to change node value at the receiver's topic registry (/topics/" + message.getReceiver() + "/" + message.getSender() + "): " + e);
        }


        return true;

    }

    private void createTopicRegistryConversation(String username1, String username2) throws KeeperException, InterruptedException {
        List<String> userTopicList = Arrays.asList(username1, username2);
        for (int i = 0; i < userTopicList.size(); i++) {
            String user1 = userTopicList.get(i);
            String user2 = userTopicList.get(userTopicList.size() - 1 - i);
            if (checkNode("/topics/" + user1 + "/" + user2) != null) break;
            zoo.create(
                    "/topics/" + user1 + "/" + user2,
                    "0".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            //Watcher
            Watcher w = we -> {
                //Wakeup the consumer of this, as a new message was received
                if (we.getType() == Watcher.Event.EventType.NodeDataChanged) {
                    new Thread(() -> {
                        kafka.receiveMessages();
                    }).start();
                }
            };

            zoo.getData("/topics/" + user1 + "/" + user2, w, null);


        }
    }

    private void handleNewTopicRegistryConversation(String username, WatchedEvent we) {

        if (checkNode("/topics/" + username) == null || checkNode(we.getPath()) == null) return;

        List<String> children = null;
        try {
            children = zoo.getChildren("/topics/" + username, null);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        kafka.subscribeConsumer(username, children);

        //Watcher
        Watcher w = innerWe -> {
            if (we.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                handleNewTopicRegistryConversation(username, innerWe);
            }
        };

        try {
            zoo.getData("/topics/" + username, w, null);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }


    }


    private void createTopicRegistry(String user) throws KeeperException, InterruptedException {

        if (checkNode("/topics/" + user) != null) return;
        zoo.create(
                "/topics/" + user,
                null,
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        //Watcher
        Watcher w = we -> {
            if (we.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                handleNewTopicRegistryConversation(user, we);
            }
        };

        zoo.getData("/topics/" + user, w, null);


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
