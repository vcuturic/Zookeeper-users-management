import { Component, OnInit } from '@angular/core';
import { ZNode } from '../../models/znode';
import { CommonModule } from '@angular/common';
import { ZookeeperService } from '../../services/zookeeper.service';
import { SingleNodeComponent } from '../single-node/single-node.component';

@Component({
  selector: 'app-node-tree',
  standalone: true,
  imports: [
    CommonModule,
    SingleNodeComponent
  ],
  templateUrl: './node-tree.component.html',
  styleUrl: './node-tree.component.css'
})
export class NodeTreeComponent implements OnInit{
  
  zNodes: ZNode[] = [];

  constructor(
    private zooKeeperService: ZookeeperService
  ) 
  {}

  ngOnInit(): void {
    this.getAllZnodesAndChildren();
  }

  getAllZnodesAndChildren() {
    this.zooKeeperService.getAllZnodesWithChildren().subscribe({
      next: (res: any) => {
        if(res) {
          const list = res[0].children;

          for (let i = 0; i < list.length; i++) {
            this.zNodes.push(list[i]);
          }

        }
      },
      error: (err: any) => {
        console.error(err);
      }
    });
  }
}
