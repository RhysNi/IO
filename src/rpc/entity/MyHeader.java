package rpc.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 12:11 上午
 */
@Data
@Builder
public class MyHeader implements Serializable {
    private static final long serialVersionUID = 5375930406718780333L;

    private int flag;
    private long requestId;
    private long dataLength;
}
