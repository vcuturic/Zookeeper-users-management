import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { AuthService } from '../services/auth.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { ButtonModule } from 'primeng/button';
import { ChatService } from '../services/chat.service';
import { LoginData } from '../models/login-data';
import * as Constants from '../constants/constants';
import * as ConnectionState from '../constants/rx-stomp-constants'
import { RxStompService } from '../services/rx-stomp.service';
import { environment } from '../../environments/environment';
import { Subscription } from 'rxjs';
import { ZNode } from '../models/znode';
import { MessageStructure } from '../models/message-structure';
import { Message } from '@stomp/stompjs';
import { ConfigurationService } from '../services/configuration.service';
import { BackendInfoService } from '../services/backend-info.service';
import { ZookeeperService } from '../services/zookeeper.service';
import { BackendService } from '../services/backend.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    InputTextModule,
    PasswordModule,
    ReactiveFormsModule,
    InputGroupModule,
    InputGroupAddonModule,
    ButtonModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit, OnDestroy{
  // users$ = this.chatService.users$;
  hidePassword = true;

  loginForm = new FormGroup({
    username: new FormControl('', ),
    password: new FormControl('', )
  });

  constructor(
    private router: Router,
    private authService: AuthService,
    // private chatService: ChatService,
    private backendService: BackendService
    ) { }

  ngOnInit(): void {
    this.backendService.init();
  }

  ngOnDestroy(): void {
    this.backendService.destruct();
  }

  login() {
    var username = this.loginForm.value.username!;

    // if (username.trim()) {
    //   this.chatService.setCurrentUser(username);
    //   if (!this.chatService.isUserExists(username)) {
    //     this.chatService.addUser(username);
    //   }
    //   // username = '';
    // }

    const loginData: LoginData = this.loginForm.value as LoginData;
    this.authService.login(loginData).subscribe({
      next: (res: any) => {
        if(res) {
          console.log(res);
        }
      },
      error: (err: any) => {
        console.error(err);
      }
      
    })
    this.authService.addUserToList(username);
    this.router.navigate(['']);
  }
}
