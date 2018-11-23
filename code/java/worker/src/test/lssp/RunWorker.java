package lssp;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Test;
import upm.lssp.exceptions.ConnectionException;
import upm.lssp.exceptions.QuitException;
import upm.lssp.exceptions.RegistrationException;
import upm.lssp.worker.ZookeeperWorker;

import static org.junit.Assert.*;


public class RunWorker {

    private static ZookeeperWorker zooWorker = null;

    private void setZooWorker(){
        if (zooWorker == null) {
            try {
                zooWorker = new ZookeeperWorker();
            } catch (ConnectionException e) {
                e.printStackTrace();
                assert (false);
            }
        }
    }


    @Test
    public void zkTest() {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.FATAL);
        System.out.println("|------ zkTest ------|");
        TestTools.connect();
        TestTools.initializeEnv();

        final String testUser1="Phil";
        setZooWorker();
        enroll(testUser1);
        doubleEnrollingPrevention(testUser1);
        //Manager
        manager_registry(testUser1);

        //Try to quit
        quit(testUser1);
        manager_quit(testUser1);

        //Let's re-enroll the user and register it
        enroll(testUser1);
        manager_registry(testUser1);
        goOnline(testUser1);

        //Now we close the connection, we shouldn't have the node anymore
        TestTools.closeConnection();
        zooWorker.goOffline();
        TestTools.connect();
        assertNull(zooWorker.checkNode("/online/"+testUser1));





        TestTools.closeConnection();
    }



    private void goOnline(String testUser1)  {
        //make a new connection
        assertTrue(zooWorker.goOnline(testUser1));
        assertNotNull(zooWorker.checkNode("/online/"+testUser1));
    }

    private void manager_quit(String testUser1) {
        assertTrue(TestTools.deleteNode("/request/quit/"+testUser1));
        assertTrue(TestTools.deleteNode("/registry/"+testUser1));
        assertNull(zooWorker.checkNode("/request/quit/"+testUser1));
        assertNull(zooWorker.checkNode("/registry/"+testUser1));
    }

    private void quit(String user) {
        System.out.println("--> quit test");
        try {
            assertTrue(zooWorker.quit(user));

        } catch (QuitException e) {
            e.printStackTrace();
            assert(false);
        }

        assertNotNull(zooWorker.checkNode("/request/quit/"+user));
    }

    private void manager_registry(String testUser1) {
        assertTrue(TestTools.deleteNode("/request/enroll/"+testUser1));
        assertTrue(TestTools.makeNode("/registry/"+testUser1,"1"));
        assertNotNull(zooWorker.checkNode("/registry/"+testUser1));

    }

    private void enroll(String user) {
        System.out.println("--> enrolling test");
        try {
            assertTrue(zooWorker.register(user));

        } catch (RegistrationException e) {
            e.printStackTrace();
            assert(false);
        }

        assertNotNull(zooWorker.checkNode("/request/enroll/"+user));


    }

    private void doubleEnrollingPrevention(String user) {
        System.out.println("--> doubleEnrolling test");
        try {
            zooWorker.register(user);
            assert(false);
        } catch (RegistrationException e) {
            assert(true);
        }
    }

}
