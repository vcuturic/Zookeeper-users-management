import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BackendInfoService {

  private backendUrlSubject = new BehaviorSubject<string>('none');
  backendUrl$ = this.backendUrlSubject.asObservable();

  constructor() {
   }

  setBackendUrl(value: string) {
    this.backendUrlSubject.next(value);
  }

  getBackendUrl(): Observable<string> {
    return this.backendUrlSubject.asObservable();
  }

}
