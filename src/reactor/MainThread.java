package reactor;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/2 12:12 上午
 */
public class MainThread {
    public static void main(String[] args) {
        //创建一个或多个IO Thread
        //boss worker都有自己的线程组
        SelectorThreadGroup boss = new SelectorThreadGroup(4);
        //混杂模式，只有一个线程负责accept,每个都会被分配client进行R/W
        SelectorThreadGroup worker = new SelectorThreadGroup(4);

        //boss得都持有worker的引用
        //boss里选一个线程注册listen ， 触发bind，从而，这个不选中的线程得持有 workerGroup的引用
        //因为未来 listen 一旦accept得到client后得去worker中 next出一个线程分配
        boss.setWorker(worker);
        //将监听`9999`端口的服务注册到某一个selector上
        boss.bind(9999);
        boss.bind(8888);
        boss.bind(7777);
        boss.bind(6666);
    }
}
