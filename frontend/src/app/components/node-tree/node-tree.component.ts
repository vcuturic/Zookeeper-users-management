import { Component, Input, OnInit } from '@angular/core';
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
export class NodeTreeComponent {
  
  @Input({required: true}) zNodes: ZNode[] = [];

  constructor(
  ) 
  {}
}
