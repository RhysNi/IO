package rpc.pool;

import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 12:31 上午
 */
public class ClientPool {
    NioSocketChannel[] clients;
    Object[] lock;

    public ClientPool(int size) {
        clients = new NioSocketChannel[size];
        lock = new Object[size];
        for (int i = 0; i < size; i++) {
            lock[i] = new Object();
        }
    }
}
