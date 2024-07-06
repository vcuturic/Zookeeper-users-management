import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  username?: string;
  userList: string[] = [];

  constructor(
    private cookieService: CookieService,
  ) { }

  isAuthenticated(): boolean
  {
    // console.log(this.username + " AUTHENTICATED: " + this.isUserInList(this.username));
    return this.cookieService.get("username") ? true : false;
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
