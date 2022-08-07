package rpc.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 4:52 上午
 */
@Data
@Builder
public class PackageMsg {
    MyHeader header;
    MsgContent content;

    public PackageMsg(MyHeader header, MsgContent content) {
        this.header = header;
        this.content = content;
    }
}
