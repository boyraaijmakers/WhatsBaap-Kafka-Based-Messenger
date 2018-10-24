package upm.lssp;

import upm.lssp.worker.ZookeeperWorker;

public class RunWorker implements Runnable {

    private static final class Lock { }
    private final Object lock = new Lock();

    public static void main(String[] args) {
        ZookeeperWorker zooWorker = new ZookeeperWorker();

        System.out.println("Enrolling node");
        String resultRegister = zooWorker.register("Boy");
        System.out.println(resultRegister);


        System.out.println("Quitting node");
        String resultQuit= zooWorker.quit("Phil");
        System.out.println(resultQuit);


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
