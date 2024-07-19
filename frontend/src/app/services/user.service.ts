import { Injectable } from '@angular/core';
import { interval, Subscription } from 'rxjs';
import { AuthService } from './auth.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BackendInfoService } from './backend-info.service';
import { LoginData } from '../models/login-data';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private heartbeatInterval = 5000; 
  private heartbeatSubscription!: Subscription;
  private subscription: Subscription;
  private username: string = this.authService.getUsername(); 
  private backendUrl: string = '';
  private userApiUrl = "user";

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private backendInfoService: BackendInfoService
  ) { 
    this.subscription = this.backendInfoService.backendUrl$.subscribe(value => {
      this.backendUrl = value;
    });

    this.startHeartbeat();
  }

  private startHeartbeat() {
    this.heartbeatSubscription = interval(this.heartbeatInterval).subscribe(() => {
      this.sendHeartbeat();
    });
  }

  sendHeartbeat() {
    this.http.post(`${this.backendUrl}/${this.userApiUrl}/heartbeat`, null, {withCredentials: true}).subscribe();
  }

  stopHeartbeat() {
    this.heartbeatSubscription.unsubscribe();
  }

  ngOnDestroy() {
    this.heartbeatSubscription.unsubscribe();
  }

  addUser(userData: LoginData) {
    return this.http.post<LoginData>(`${this.backendUrl}/${this.userApiUrl}/add`, userData);
  }

  removeUser(username: string) {
    let params = new HttpParams().set('userRemoved', username);
    return this.http.post<any>(`${this.backendUrl}/${this.userApiUrl}/removeUser`, null, {params})
  }
}
