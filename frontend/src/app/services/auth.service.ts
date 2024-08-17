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

  isAuthenticated(): boolean
  {
    return localStorage.getItem("username") ? true : false;
  }

  login(userData: User) {
    return this.http.post<LoginData>(`${this.backendUrl}/${this.authApiUrl}/login`, userData);
  }

  deleteCookies() {
    localStorage.removeItem("username");
    this.removeUserFromList(this.username!);
  }

  logout() {
    const username = localStorage.getItem("username");
    return this.http.post<any>(`${this.backendUrl}/${this.authApiUrl}/logout`, username);
  }

  getUsername(): string {
    return localStorage.getItem("username")!;
  }

  saveListToCookies() {
    this.cookieService.set('userList', JSON.stringify(this.userList));
  }

  loadListFromCookies() {
    const userListString = this.cookieService.get('userList');

    if (userListString) {
      this.userList = JSON.parse(userListString);
    }
  }

  addUserToList(user: string) {
    this.userList.push(user);
    // this.username = user;
    localStorage.setItem("username", user);
    // this.cookieService.set("username", user, { secure: true, sameSite: "Strict"  });
    // this.cookieService.setHttpOnly(true);
    // this.saveListToCookies();
  }

  removeUserFromList(username: string) {
    const index = this.userList.indexOf(username);
    if (index !== -1) {
        this.userList.splice(index, 1);
        this.saveListToCookies();
    }
  }

  isUserInList(username?: string): boolean {
    if(!username)
      return false;

    const index = this.userList.indexOf(username);

    if (index !== -1) {
        return true;
    }

    return false;
  }
}
