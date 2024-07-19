package com.example.zookeeperusersnodes.constants;

public final class NodeOperations {

    public NodeOperations() {

    }

    public static final String OPERATION_DELETE = "delete";
    public static final String OPERATION_DISCONNECT = "disconnected";
    // Connected - Make a new zNode
    // - offline - don't make it in live_nodes
    // - online - make it in live_nodes
    public static final String OPERATION_CONNECT_OFFLINE = "connected_offline";
    public static final String OPERATION_CONNECT_ONLINE = "connected_online";
    public static final String OPERATION_RECONNECT = "reconnected";
}
