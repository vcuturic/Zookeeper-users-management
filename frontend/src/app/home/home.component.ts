import { Component, OnInit } from '@angular/core';
import { HomeService } from '../services/home.service';
import { CommonModule } from '@angular/common';
import { ZNode } from '../models/znode';
import { ZookeeperService } from '../services/zookeeper.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit{

  response: any;
  zNodes?: ZNode[];

  constructor(
    private zookeeperService: ZookeeperService
    ) {}

  ngOnInit(): void {
    this.getAllZnodes();
  }

  getAllZnodes() {
    this.zookeeperService.getAllZnodes().subscribe({
      next: (res: ZNode[]) => {
        if(res)
          this.zNodes = res;
      },
      error: (err: any) => {
        console.error(err);
      }
    });
  }
}
