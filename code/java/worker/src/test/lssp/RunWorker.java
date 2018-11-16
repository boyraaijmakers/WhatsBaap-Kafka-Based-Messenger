package lssp;


import org.junit.Test;

import upm.lssp.worker.ZookeeperWorker;

import static junit.framework.Assert.*;


public class RunWorker {
    @Test
    public void zkTest() {
        ZookeeperWorker zooWorker = new ZookeeperWorker();

        System.out.println("Enrolling node");
        String resultRegister = zooWorker.register("Philo");

        assertEquals(resultRegister,"Philo already has a pending enrollment request!");


        /*System.out.println("Quitting node");
        String resultQuit= zooWorker.quit("Phil");
        System.out.println(resultQuit);
*/

    }
}
