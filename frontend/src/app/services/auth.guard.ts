import { inject } from "@angular/core";
import { AuthService } from "./auth.service";
import { Router } from "@angular/router";

export const CanActivate = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated())
    {
        router.navigate(['login'])
        return false;
    }
    else {
        return true;
    }
}