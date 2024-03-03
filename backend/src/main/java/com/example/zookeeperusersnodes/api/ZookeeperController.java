package com.example.zookeeperusersnodes.api;

import com.example.zookeeperusersnodes.bl.ZooKeeperBL;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/zookeeper")
public class ZookeeperController {
    private final ZooKeeperBL zooKeeperBL;

    public ZookeeperController(ZooKeeperBL zooKeeperBL) {
        this.zooKeeperBL = zooKeeperBL;
    }
    @GetMapping("/servernodes")
    public List<NodeDTO> getZookeeperServerNodes() {
        return this.zooKeeperBL.getZookeeperServerNodes();
    }
}
