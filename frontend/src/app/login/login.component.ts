import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { ButtonModule } from 'primeng/button';
import { LoginData } from '../models/login-data';
import { BackendService } from '../services/backend.service';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { BackendInfoService } from '../services/backend-info.service';
import { LoadingComponent } from '../components/loading/loading.component';
import { User } from '../models/user';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    InputTextModule,
    PasswordModule,
    ReactiveFormsModule,
    InputGroupModule,
    InputGroupAddonModule,
    ButtonModule,
    CommonModule,
    LoadingComponent
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit, OnDestroy{
  hidePassword = true;
  loading: boolean = true;
  private subscription: Subscription;

  loginForm = new FormGroup({
    username: new FormControl('', ),
    password: new FormControl('', )
  });

  constructor(
    private router: Router,
    private authService: AuthService,
    private backendService: BackendService,
    private backendInfoService: BackendInfoService
    ) { 
      this.subscription = this.backendInfoService.backendUrl$.subscribe(value => {
        if(value !== "none")
          this.loading = false;
      });
    }

  ngOnInit(): void {
    this.backendService.init();
  }

  ngOnDestroy(): void {
    // this.backendService.destruct();
    this.subscription.unsubscribe();
  }

  login() {
    var username = this.loginForm.value.username!;

    const userData: User = this.loginForm.value as User;
    userData.online = true;  
    userData.address = window.location.origin; 

    console.log("userData: ");
    console.log(userData);

    this.authService.login(userData).subscribe({
      next: (res: any) => {
        if(res) {
          console.log(res);
        }
      },
      error: (err: any) => {
        console.error(err);
      },
      complete: () => {
        this.router.navigate(['']);
      }
      
    })
    this.authService.addUserToList(username);
  }
}
