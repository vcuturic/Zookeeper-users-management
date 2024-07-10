package com.example.zookeeperusersnodes.models;

import com.example.zookeeperusersnodes.dto.NodeDTO;

public class Message {
    public String operation;
    public NodeDTO zNode;

    public Message(String operation, NodeDTO zNode) {
        this.operation = operation;
        this.zNode = zNode;
    }
}
