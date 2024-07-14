import { Injectable } from '@angular/core';
import { interval, Subscription } from 'rxjs';
import { AuthService } from './auth.service';
import { HttpClient } from '@angular/common/http';
import { BackendInfoService } from './backend-info.service';

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

  private sendHeartbeat() {
    this.http.post(`${this.backendUrl}/${this.userApiUrl}/heartbeat`, null, {withCredentials: true}).subscribe();
  }

  ngOnDestroy() {
    this.heartbeatSubscription.unsubscribe();
    this.sendUserLeft();
  }

  private sendUserLeft() {
    navigator.sendBeacon(`${this.backendUrl}/${this.userApiUrl}/user-left`, JSON.stringify({ username: this.username, password: "" }));
  }

  logout() {
    this.heartbeatSubscription.unsubscribe();
    return this.http.post<any>(`${this.backendUrl}/${this.userApiUrl}/logout`, null, { withCredentials: true });
  }
}
