package lssp;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import upm.lssp.exceptions.RegistrationException;
import upm.lssp.worker.ZookeeperWorker;

import static org.junit.Assert.*;


public class RunWorker {

    private static ZookeeperWorker zooWorker = null;

    private void setZooWorker(){
        if(zooWorker==null) zooWorker=new ZookeeperWorker();
    }


    @Test
    public void zkTest() {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.FATAL);
        System.out.println("|------ zkTest ------|");
        TestTools.initializeEnv();

        setZooWorker();
        enrolling();
        doubleEnrolling();


    }

    private void enrolling() {
        System.out.println("--> enrolling test");
        try {
            assertTrue(zooWorker.register("Phil"));

        } catch (RegistrationException e) {
            e.printStackTrace();
            assert(false);
        }

        assertNotNull(zooWorker.checkNode("/request/enroll/Phil"));


    }
    private void doubleEnrolling() {
        System.out.println("--> doubleEnrolling test");
        try {
            zooWorker.register("Phil");
            assert(false);
        } catch (RegistrationException e) {
            assert(true);
        }
    }

}
