import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { AuthService } from '../services/auth.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { ButtonModule } from 'primeng/button';

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
export class LoginComponent {
  hidePassword = true;

  loginForm = new FormGroup({
    username: new FormControl('', ),
    password: new FormControl('', )
  });

  constructor(
    private cookie: CookieService,
    private router: Router,
    private authService: AuthService
    ) { }

  ngOnInit(): void {
  }

  login() {
    this.authService.addUserToList(this.loginForm.value.username!);
    this.router.navigate(['']);
  }
}
