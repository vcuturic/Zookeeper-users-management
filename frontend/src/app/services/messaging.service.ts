import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UserMessage } from '../models/user-message';
import { BehaviorSubject, Subscription } from 'rxjs';
import { BackendInfoService } from './backend-info.service';
import { RxStompService } from './rx-stomp.service';

@Injectable({
  providedIn: 'root'
})
export class MessagingService {

  private backendUrl: string = '';
  private subscription: Subscription;
  private messageApiUrl = "message";
  private messagesSubject = new BehaviorSubject<UserMessage[]>([]);
  messages$ = this.messagesSubject.asObservable();

  constructor(
    private http: HttpClient,
    private backendInfoService: BackendInfoService,
    private rxStompService: RxStompService
  ) { 
    this.subscription = this.backendInfoService.backendUrl$.subscribe(value => {
      this.backendUrl = value;
    });

    this.rxStompService.watch('/topic/messages').subscribe((message) => {
      const parsedMessage: UserMessage = JSON.parse(message.body);
      const currentMessages = this.messagesSubject.getValue();
      this.messagesSubject.next([...currentMessages, parsedMessage]);
    });
  }

  sendMessage(userMessage: UserMessage) {
    const headers = new HttpHeaders().set('request-from', window.location.origin);
    return this.http.post<UserMessage>(`${this.backendUrl}/${this.messageApiUrl}/receive`, userMessage, {headers});
  }
}
