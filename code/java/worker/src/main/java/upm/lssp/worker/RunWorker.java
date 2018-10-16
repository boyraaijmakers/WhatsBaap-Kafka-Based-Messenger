package upm.lssp.worker;

public class RunWorker {

    public static void main(String[] args) {
        ZookeeperWorker zooWorker = new ZookeeperWorker();

        String result = zooWorker.register("Phil");

        System.out.println(result);
    }

}
