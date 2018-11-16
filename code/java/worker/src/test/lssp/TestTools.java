package lssp;

import org.apache.zookeeper.*;
import upm.lssp.worker.ZookeeperWorker;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static upm.lssp.Config.*;

public class TestTools {

    private final static List<String> structure= Arrays.asList("/request","/registry","/online","/request/enroll","/request/quit");


    public static void initializeEnv(){
        ZookeeperWorker zk = new ZookeeperWorker();
        final CountDownLatch connectionLatch = new CountDownLatch(1);
        try {
            ZooKeeper zoo = new ZooKeeper(ZKSERVER, ZKSESSIONTIME, new Watcher() {
                public void process(WatchedEvent we) {
                    if (we.getState() == Event.KeeperState.SyncConnected) {
                        connectionLatch.countDown();
                    }
                }
            });
            for (String node: structure) {

                try {
                    ZKUtil.deleteRecursive(zoo, node);
                }catch(KeeperException.NoNodeException e){
                    // Proceed anyway
                }

                zoo.create(
                        node,
                        node.replaceAll(".*/", "").getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);


            }
            connectionLatch.await(10, TimeUnit.SECONDS);
        }catch(Exception e){
            e.printStackTrace();
            assert(false);
        }
    }
}
