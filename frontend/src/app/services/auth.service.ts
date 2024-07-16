import { Injectable, OnDestroy } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { LoginData } from '../models/login-data';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BackendInfoService } from './backend-info.service';
import { Subscription } from 'rxjs';

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
    return this.cookieService.get("username") ? true : false;
  }

  login(loginData: LoginData, userAddedParam: boolean = false) {
    let params = new HttpParams().set('userAdded', userAddedParam);
    return this.http.post<LoginData>(`${this.backendUrl}/${this.authApiUrl}/login`, loginData, {params});
  }

  deleteCookies() {
    this.cookieService.delete("username");
    this.removeUserFromList(this.username!);
  }

  getUsername(): string {
    return this.cookieService.get("username");
  }

  // Function to store the list in cookies
  saveListToCookies() {
    // Convert the list to JSON and store it in a cookie named 'userList'
    this.cookieService.set('userList', JSON.stringify(this.userList));
  }

  // Function to retrieve the list from cookies
  loadListFromCookies() {
    // Retrieve the JSON string from the cookie
    const userListString = this.cookieService.get('userList');
    // If the cookie exists and is not empty
    if (userListString) {
      // Parse the JSON string back to a list of strings
      this.userList = JSON.parse(userListString);
      console.log(this.userList);
    }
  }

  addUserToList(user: string) {
    this.userList.push(user);
    // this.username = user;
    this.cookieService.set("username", user);
    this.saveListToCookies();
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
