import { Component, Input } from '@angular/core';
import { ZNode } from '../../models/znode';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-nodes',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './nodes.component.html',
  styleUrl: './nodes.component.css'
})
export class NodesComponent {

  @Input() zNodes?: ZNode[];

}
