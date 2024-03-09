import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ZNode } from '../../models/znode';

@Component({
  selector: 'app-single-node',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './single-node.component.html',
  styleUrl: './single-node.component.css'
})
export class SingleNodeComponent {
  @Input({required: true}) zNode!: ZNode;

  expanded = false;

  toggleChildrenVisibility(zNode: ZNode): void {
    if(zNode.children)
      this.expanded = !this.expanded;
  }
}
