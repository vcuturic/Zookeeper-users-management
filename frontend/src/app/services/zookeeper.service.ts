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


  getAllZnodesWithChildren(backEndUrl: string): Observable<any> {
    return this.http.get<any>(`${backEndUrl}/${this.zookeeperApiUrl}/servernodes`)
  }

  getAllNodesChildren(backEndUrl: string): Observable<any> {
    return this.http.get<any>(`${backEndUrl}/${this.zookeeperApiUrl}/allnodes`);
  }

  getAllNodesChildrenInfo(backEndUrl: string): Observable<any> {
    return this.http.get<any>(`${backEndUrl}/${this.zookeeperApiUrl}/allnodesinfo`);
  }

  getLiveNodesChildren(backEndUrl: string): Observable<any> {
    return this.http.get<any>(`${backEndUrl}/${this.zookeeperApiUrl}/livenodes`);
  }
}
