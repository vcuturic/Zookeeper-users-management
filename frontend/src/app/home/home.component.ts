import { Component } from '@angular/core';
import { NodeTreeComponent } from '../components/node-tree/node-tree.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NodeTreeComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

  constructor() {}

}
