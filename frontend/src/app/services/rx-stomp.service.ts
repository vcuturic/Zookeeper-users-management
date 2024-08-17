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
            // this.currentUrlIndex = (this.currentUrlIndex + 1) % this.websocketUrls.length;
            // const currentUrl = this.websocketUrls[this.currentUrlIndex];
            const currentUrl = this.websocketUrls[this.getWebSocketUrlByPort()];
            this.connectingSubject.next(currentUrl);
            client.configure({brokerURL: currentUrl});
            resolve();
        });
    };
  }

  onConnecting(): Observable<string> {
    return this.connectingSubject.asObservable();
  }

  getWebSocketUrlByPort(): number {
    const currentPort = window.location.port;

    if(currentPort === "4200")
      return 0;
    if(currentPort === "4201")
      return 1;
    if(currentPort === "4202")
      return 2;

    return -1;
  }
}
