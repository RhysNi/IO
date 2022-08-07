package rpc.handler;

import rpc.entity.PackageMsg;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/8/8 2:09 上午
 */
public class ResponseMappingCallBack {
    static ConcurrentHashMap<Long, CompletableFuture> mapping = new ConcurrentHashMap<>();

    public static void addCallBack(long requestId, CompletableFuture callBack) {
        mapping.putIfAbsent(requestId, callBack);
    }

    public static void runCallBack(PackageMsg packageMsg) {
        CompletableFuture callBack = mapping.get(packageMsg.getHeader().getRequestId());
        callBack.complete(packageMsg.getContent().getRes());
        removeCallBack(packageMsg.getHeader().getRequestId());
    }

    private static void removeCallBack(long requestId) {
        mapping.remove(requestId);
    }
}
