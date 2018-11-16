package lssp;


import org.junit.Test;

import upm.lssp.exceptions.RegistrationException;
import upm.lssp.worker.ZookeeperWorker;

import static junit.framework.Assert.*;


public class RunWorker {
    @Test
    public void zkTest() {
        System.out.println("|------ zkTest ------|");


        TestTools.initializeEnv();
        ZookeeperWorker zooWorker = new ZookeeperWorker();

        /*System.out.println("Enrolling node");
        String resultRegister = null;
        try {
            zooWorker.register("Philo");
            assert(true);
        } catch (RegistrationException e) {
            e.printStackTrace();
            assert(false);
        }*

        //assertEquals(resultRegister,"Philo already has a pending enrollment request!");


        /*System.out.println("Quitting node");
        String resultQuit= zooWorker.quit("Phil");
        System.out.println(resultQuit);
*/

    }
}
