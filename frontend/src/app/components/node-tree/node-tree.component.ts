import { Component, OnInit } from '@angular/core';
import { ZNode } from '../../models/znode';
import { CommonModule } from '@angular/common';
import { TreeModule } from 'primeng/tree';
import { TreeNode } from 'primeng/api';
import { ZookeeperService } from '../../services/zookeeper.service';
import { PanelModule } from 'primeng/panel';

@Component({
  selector: 'app-node-tree',
  standalone: true,
  imports: [
    CommonModule,
    TreeModule,
    PanelModule
  ],
  templateUrl: './node-tree.component.html',
  styleUrl: './node-tree.component.css'
})
export class NodeTreeComponent implements OnInit{
  
  zNodes: ZNode[] = [];
  files: TreeNode[] = [];

  treeNodeList: TreeNode[] = [];

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
      },
      complete: () => {
        this.treeNodeList = this.convertToTreeNodes(this.zNodes);
      }
    });
  }

  convertToTreeNodes(zNodes: ZNode[]): TreeNode[] {
    return zNodes.map(zNode => this.convertNode(zNode));
  }

  convertNode(zNode: ZNode): TreeNode {
    const treeNode: TreeNode = {
      label: zNode.name,
      icon: "pi pi-fw pi-database", 
      children: []
    };

    if (zNode.children && zNode.children.length > 0) {
      treeNode.children = this.convertToTreeNodes(zNode.children);
    }

    return treeNode;
  }
}
