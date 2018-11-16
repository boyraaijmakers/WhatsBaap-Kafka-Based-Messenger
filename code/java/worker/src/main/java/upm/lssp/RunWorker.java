package upm.lssp;

import upm.lssp.exceptions.RegistrationException;
import upm.lssp.worker.ZookeeperWorker;

public class RunWorker implements Runnable {

    private static final class Lock { }
    private final Object lock = new Lock();

    public static void main(String[] args) {
        ZookeeperWorker zooWorker = new ZookeeperWorker();




        RunWorker rw = new RunWorker();
        rw.run();
    }


    public void run() {
        synchronized (lock) {
            while (true) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
