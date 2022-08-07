package rpc.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 5:36 上午
 */
public class SerDerUtil {
    static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public synchronized static byte[] ser(Object msg) {
        //清空byteArrayOutputStream
        byteArrayOutputStream.reset();
        ObjectOutputStream objectOutputStream;
        byte[] msgBody = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(msg);
            msgBody = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msgBody;
    }
}
