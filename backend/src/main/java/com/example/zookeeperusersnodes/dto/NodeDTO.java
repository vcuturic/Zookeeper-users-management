package com.example.zookeeperusersnodes.dto;
import com.example.zookeeperusersnodes.constants.NodeTypes;

import java.util.List;

public class NodeDTO {
    public String name;
    private String data;
    public List<NodeDTO> children;
    public boolean online = true;
    public int type = NodeTypes.ZNODE_TYPE_INITIAL;
    public String status; // Used to track what happened to user(CONNECTED, RECONNECTED, DISCONNECTED)

    public NodeDTO(String name, String data, List<NodeDTO> children, boolean online) {
        this.name = name;
        this.data = data;
        this.children = children;
        this.online = online;
    }

    public NodeDTO(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public NodeDTO(String name, int type, String status) {
        this.name = name;
        this.type = type;
        this.status = status;
    }

    public NodeDTO() {

    }

    public NodeDTO(String name, String data, List<NodeDTO> children) {
        this.name = name;
        this.data = data;
        this.children = children;
    }

    public NodeDTO(String name, int type, boolean online) {
        this.name = name;
        this.type = type;
        this.online = online;
    }
}
