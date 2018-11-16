package lssp;

import org.apache.zookeeper.*;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static upm.lssp.Config.*;

public class TestTools {

    private static final List<String> structure= Arrays.asList("/request","/registry","/online","/request/enroll","/request/quit");
    private static ZooKeeper zoo=null;

    protected static void connect(){
        try {
            zoo=connectionFactory();
        }catch(Exception e){
            e.printStackTrace();
            assert(false);
        }


    }
    protected static ZooKeeper connectionFactory() throws Exception{
        final CountDownLatch connectionLatch = new CountDownLatch(1);
        ZooKeeper zoo_toReturn = new ZooKeeper(ZKSERVER, ZKSESSIONTIME, new Watcher() {
            public void process(WatchedEvent we) {
                if (we.getState() == Event.KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
            }
        });
        connectionLatch.await(10, TimeUnit.SECONDS);

        return zoo_toReturn;

    }

    protected static void initializeEnv(){

        for (String node: structure) {

            deleteNode(node);



            makeNode(node,node.replaceAll(".*/", ""));

        }
    }
    protected static ZooKeeper getTestZoo(){
        if(zoo==null) connect();
        return zoo;
    }
    protected static boolean editNode(String node, String value) {
        if(zoo==null) connect();
        try {
            zoo.setData(node, value.getBytes(), 1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            assert (false);
            return false;
        }
    }
    protected static boolean deleteNode(String node) {
        if(zoo==null) connect();

        try {


            ZKUtil.deleteRecursive(zoo, node);
            return true;
        } catch (Exception e) {
            //Not exists, ok anyway
        }
        return false;
    }
    protected static boolean makeNode(String node, String value) {
        if(zoo==null) connect();
        try {
            zoo.create(
                    node,
                    value.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    protected static void closeConnection(){
        try {
            zoo.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
