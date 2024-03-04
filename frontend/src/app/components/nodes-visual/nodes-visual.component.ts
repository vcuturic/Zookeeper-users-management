import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ZNode } from '../../models/znode';

@Component({
  selector: 'app-nodes-visual',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './nodes-visual.component.html',
  styleUrl: './nodes-visual.component.css'
})
export class NodesVisualComponent {
  @Input({required: true}) zNodes!: ZNode[]; 

}
