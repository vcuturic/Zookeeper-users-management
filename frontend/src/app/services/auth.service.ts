import { Injectable, OnDestroy } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { LoginData } from '../models/login-data';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BackendInfoService } from './backend-info.service';
import { Subscription } from 'rxjs';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class AuthService implements OnDestroy{

  username?: string;
  userList: string[] = [];
  
  private backendUrl: string = '';
  private subscription: Subscription;
  private authApiUrl = "auth";

  constructor(
    private cookieService: CookieService,
    private http: HttpClient,
    private backendInfoService: BackendInfoService
  ) { 
    this.subscription = this.backendInfoService.backendUrl$.subscribe(value => {
      this.backendUrl = value;
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  isAuthenticated(): boolean {
    return localStorage.getItem("username") ? true : false;
  }

  login(userData: User) {
    this.setUserData(userData);
    return this.http.post<LoginData>(`${this.backendUrl}/${this.authApiUrl}/login`, userData);
  }

  deleteCookies() {
    localStorage.removeItem("username");
    localStorage.removeItem("userData");
  }

  logout() {
    const username = localStorage.getItem("username");
    return this.http.post<any>(`${this.backendUrl}/${this.authApiUrl}/logout`, username);
  }

  getUsername(): string {
    return localStorage.getItem("username")!;
  }

  setUserData(userData: User) {
    localStorage.setItem("username", userData.username);
    localStorage.setItem("userData", JSON.stringify(userData));
  }

  getUserData(): User {
    const userData: User = JSON.parse(localStorage.getItem("userData")!);
    return userData;
  }
}
