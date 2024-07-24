package com.example.zookeeperusersnodes.api;
import com.example.zookeeperusersnodes.dto.NodeDTO;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/zookeeper")
public class ZookeeperController {
    private final ZooKeeperService zooKeeperService;

    public ZookeeperController(ZooKeeperService zooKeeperService) {
        this.zooKeeperService = zooKeeperService;
    }
    @GetMapping("/serverNodes")
    public List<NodeDTO> getZookeeperServerNodes() {
        return this.zooKeeperService.getZookeeperServerNodes();
    }

    @GetMapping("/allNodes")
    public List<NodeDTO> getAllNodesChildren() {
        return this.zooKeeperService.getAllNodesChildren();
    }

    @GetMapping("/liveNodes")
    public List<String> getLiveNodesChildren() {
        return this.zooKeeperService.getLiveNodesChildren();
    }

    @GetMapping("/availability")
    public ResponseEntity<ServerResponseDTO> checkServerAvailability() {
        return ResponseEntity.ok(new ServerResponseDTO("Server is available"));
    }
}
