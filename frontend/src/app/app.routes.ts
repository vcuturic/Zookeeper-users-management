import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { CanActivate } from './services/auth.guard';
import { LoginComponent } from './login/login.component';

export const routes: Routes = [
    { path: "", component: HomeComponent, canActivate: [CanActivate]},
    { path: "login", component: LoginComponent}
];
