import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { RxStompService } from './rx-stomp.service';
import { AuthService } from './auth.service';

interface ChatMessage {
  sender: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  // private messagesSubject = new BehaviorSubject<string[]>([]);
  private usersSubject = new BehaviorSubject<string[]>(['Alice', 'Bob', 'Charlie']);
  private currentUserSubject = new BehaviorSubject<string>('');

  // messages$ = this.messagesSubject.asObservable();
  users$ = this.usersSubject.asObservable();
  // currentUser$ = this.currentUserSubject.asObservable();

  // constructor(private rxStompService: RxStompService) {
  //   this.rxStompService.watch('/topic/messages').subscribe((message) => {
  //     const parsedMessage = JSON.parse(message.body);
  //     const currentMessages = this.messagesSubject.getValue();
  //     this.messagesSubject.next([...currentMessages, parsedMessage]);
  //   });
  // }

  // sendMessage(message: string) {
  //   // const currentMessages = this.messagesSubject.getValue();
  //   const currentUser = this.currentUserSubject.getValue();
  //   const fullMessage = { sender: currentUser, message: message };
  //   // this.messagesSubject.next([...currentMessages, `${currentUser}: ${message}`]);
  //   this.rxStompService.publish({ destination: '/topic/messages', body: JSON.stringify(fullMessage) });
  // }

  // getUsername() {
  //   return this.currentUserSubject.getValue();
  // }
  private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
  messages$ = this.messagesSubject.asObservable();

  private username: string;

  constructor(
    private rxStompService: RxStompService,
    private authService: AuthService
  ) {
    // TODO ima neki LOADING problem, odmah posle logina username je blank, verovatno se prvo ucita servis pa pokupi blank
    // nakon reloada radi sve...
    // ubaciti neku loading animaciju, sacekati da se konektuje na backend i websocket! :)
    // TODO: NE TREBA ovako da radi, potrebno je da se okida watcher kada user posalje poruku
    // TODO: znaci prevezati, user1 poruka > backend (okida se watcher) > obavestenje user2 da je poruka stigla
    // watcher da bi se okinuo, mora postojati citav znode za poruke recimo....
    // mora login otici na backend, i onda da se u all usere i live_user-e ubacuju useri sa fronta
    // pa isti watcher koji se okida kada se dodaju serveri u all_nodes i live_nodes, tako da se dodaju
    // i korisnici sa fronta, mislim da je to poprilicno to
    this.username = this.authService.getUsername();
    console.log("DBG: " + this.username)

    this.rxStompService.watch('/topic/messages').subscribe((message) => {
      const parsedMessage: ChatMessage = JSON.parse(message.body);
      const currentMessages = this.messagesSubject.getValue();
      this.messagesSubject.next([...currentMessages, parsedMessage]);
    });
  }

  sendMessage(message: string) {
    const fullMessage: ChatMessage = { sender: this.username, message: message };
    // this.rxStompService.publish({ destination: '/topic/messages', body: JSON.stringify(fullMessage) });
  }

  getUsername() {
    return this.username;
  }

  addUser(username: string) {
    const currentUsers = this.usersSubject.getValue();
    this.usersSubject.next([...currentUsers, username]);
  }

  setCurrentUser(username: string) {
    this.currentUserSubject.next(username);
  }

  isUserExists(username: string): boolean {
    return this.usersSubject.getValue().includes(username);
  }
}
