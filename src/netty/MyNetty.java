package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;

import static java.lang.System.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/4 11:04 下午
 */
public class MyNetty {
    
    @Test
    public void  myByteBuf(){
        //分配器
        //ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(8, 20);

        //非池化分配器
        //ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);

        //池化分配器
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);

        print(buffer);

        buffer.writeBytes(new byte[]{1,2,3,4});
        print(buffer);

        buffer.writeBytes(new byte[]{1,2,3,4});
        print(buffer);

        buffer.writeBytes(new byte[]{1,2,3,4});
        print(buffer);

        buffer.writeBytes(new byte[]{1,2,3,4});
        print(buffer);

        buffer.writeBytes(new byte[]{1,2,3,4});
        print(buffer);

        // 超过最大分配上限 IndexOutOfBoundsException
        // buffer.writeBytes(new byte[]{1,2,3,4});
        // print(buffer);
    }

    public static void print(ByteBuf buf){
        //是否可读
        out.println("buf.isReadable(是否可读): "+ buf.isReadable());
        //从哪里开始读
        out.println("buf.readerIndex(启读索引): "+ buf.readerIndex());
        //可读的字节数
        out.println("buf.readableBytes(可读字节数): "+ buf.readableBytes());
        //是否可写
        out.println("buf.isWritable(是否可写): "+ buf.isWritable());
        //从哪里开始写
        out.println("buf.writerIndex(启写索引): "+ buf.writerIndex());
        //可写字节数
        out.println("buf.writableBytes(可写字节数): "+ buf.writableBytes());
        //动态分配空间大小
        out.println("buf.capacity(动态分配上限): "+ buf.capacity());
        //最大动态分配空间大小
        out.println("buf.maxCapacity(最大动态分配上限): "+ buf.maxCapacity());
        //堆内还是堆外分配 true:堆外  false:堆内
        out.println("buf.isDirect(是否堆外分配): "+ buf.isDirect());

        out.println("===========================================");
    }
}
