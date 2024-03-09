import { Injectable } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { environment } from '../../environments/environment';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RxStompService extends RxStomp{
  private websocketUrls = [environment.websocketUrl1, environment.websocketUrl2, environment.websocketUrl3];
  private currentUrlIndex = -1;
  private connectingSubject = new Subject<string>();
  
  constructor() {
    super();
    this.configureStomp();
  }

  private configureStomp() {

    this._beforeConnect = (client: any): Promise<void> => {
        return new Promise<void>((resolve, _) => {
            this.currentUrlIndex = (this.currentUrlIndex + 1) % this.websocketUrls.length;
            const currentUrl = this.websocketUrls[this.currentUrlIndex];
            this.connectingSubject.next(currentUrl);
            client.configure({brokerURL: currentUrl});
            resolve();
        });
    };
  }

  onConnecting(): Observable<string> {
    return this.connectingSubject.asObservable();
  }
}
