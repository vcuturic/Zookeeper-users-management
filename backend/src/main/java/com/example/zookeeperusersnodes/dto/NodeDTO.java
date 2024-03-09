package com.example.zookeeperusersnodes.dto;
import java.util.List;

public class NodeDTO {
    public String name;
    private String data;
    public List<NodeDTO> children;
    public boolean online = true;

    public NodeDTO(String name, String data, List<NodeDTO> children, boolean online) {
        this.name = name;
        this.data = data;
        this.children = children;
        this.online = online;
    }

    public NodeDTO(String name, String data, List<NodeDTO> children) {
        this.name = name;
        this.data = data;
        this.children = children;
    }
}
