package reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/2 12:15 上午
 */
public class SelectorThreadGroup {
    //开辟一个数组空间存放线程
    SelectorThread[] selectorThreads;
    ServerSocketChannel server;
    AtomicInteger xid = new AtomicInteger(0);

    SelectorThreadGroup group = this;

    public SelectorThreadGroup(int threadNum) {
        selectorThreads = new SelectorThread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            selectorThreads[i] = new SelectorThread(this);
            new Thread(selectorThreads[i]).start();
        }
    }

    /**
     * 端口绑定
     *
     * @param port
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/2
     */
    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

         //注册到哪个selector上
         // nextSelector(server);
         // nextSelectorV2(server);
            nextSelectorV3(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 无论serverSocket 还是socket 都可复用
     *
     * @param channel
     * @return void
     * @author Rhys.Ni
     * @date 2022/8/2
     */
    public void nextSelector(Channel channel) {
        SelectorThread selectorThread = next();
        //通过队列传输数据
        selectorThread.lbq.add(channel);
        //打断阻塞，让对应的线程打断后自己去完成注册selector
        selectorThread.selector.wakeup();
    }

    public void nextSelectorV2(Channel channel) {
        try {

            if (channel instanceof ServerSocketChannel) {
                selectorThreads[0].lbq.put(channel);
                selectorThreads[0].selector.wakeup();

            } else {
                SelectorThread selectorThread = nextV2();

                //通过队列传输数据
                selectorThread.lbq.add(channel);
                //打断阻塞，让对应的线程打断后自己去完成注册selector
                selectorThread.selector.wakeup();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void nextSelectorV3(Channel channel) {
        try {
            if (channel instanceof ServerSocketChannel) {
                SelectorThread selectorThread = next();
                //通过队列传输数据
                selectorThread.lbq.put(channel);
                selectorThread.setWorker(group);
                //打断阻塞，让对应的线程打断后自己去完成注册selector
                selectorThread.selector.wakeup();
            } else {
                SelectorThread selectorThread = nextV3();
                //通过队列传输数据
                selectorThread.lbq.add(channel);
                //打断阻塞，让对应的线程打断后自己去完成注册selector
                selectorThread.selector.wakeup();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 线程分配
     *
     * @param
     * @return reactor.SelectorThread
     * @author Rhys.Ni
     * @date 2022/8/2
     */
    private SelectorThread next() {
        int index = xid.incrementAndGet() % selectorThreads.length;
        return selectorThreads[index];
    }

    private SelectorThread nextV2() {
        int index = xid.incrementAndGet() % (selectorThreads.length - 1);
        return selectorThreads[index + 1];
    }

    /**
     * 对worker进行线程分配
     * @author Rhys.Ni
     * @date 2022/8/2
     * @param
     * @return reactor.SelectorThread
     */
    private SelectorThread nextV3() {
        int index = xid.incrementAndGet() % group.selectorThreads.length;
        return selectorThreads[index];
    }

    public void setWorker(SelectorThreadGroup worker) {
        this.group = worker;
    }
}
