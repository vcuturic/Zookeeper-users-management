import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {

  constructor(private http: HttpClient) { }

  checkForAvailableBackendUrl(backEndUrl: string): Observable<any> {
    return this.http.get<any>(`${backEndUrl}/zookeeper/availability`);
  }
}
