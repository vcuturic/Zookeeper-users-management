package com.example.zookeeperusersnodes.bl;

import com.example.zookeeperusersnodes.dto.NodeDTO;

import java.util.List;

public interface ZooKeeperBL {
    List<NodeDTO> getZookeeperServerNodes();
}
