import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ZNode } from '../models/znode';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ZookeeperService {

  private zookeeperApiUrl = "zookeeper";

  constructor(private http: HttpClient) { }

  getAllZnodes() : Observable<ZNode[]> {
    return this.http.get<ZNode[]>(`${environment.apiUrl}/${this.zookeeperApiUrl}/nodes`);
  }
}
