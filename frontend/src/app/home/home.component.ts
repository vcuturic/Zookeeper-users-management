import { Component, OnInit } from '@angular/core';
import { NodeTreeComponent } from '../components/node-tree/node-tree.component';
import { NodesVisualComponent } from '../components/nodes-visual/nodes-visual.component';
import { ZNode } from '../models/znode';
import { ZookeeperService } from '../services/zookeeper.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NodeTreeComponent,
    NodesVisualComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit{

  zNodes: ZNode[] = [];

  constructor(
    private zooKeeperService: ZookeeperService
    ) {}

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
