package rpc.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/7 11:49 下午
 */
@Data
@Builder
public class MsgContent implements Serializable {
    private static final long serialVersionUID = -4292598326302464485L;

    String name;
    String methodName;
    Class<?>[] parameterTypes;
    Object[] args;
    String res;
}
