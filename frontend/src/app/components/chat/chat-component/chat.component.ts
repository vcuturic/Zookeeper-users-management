import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { MessagingService } from '../../../services/messaging.service';
import { UserMessage } from '../../../models/user-message';
import { ZNode } from '../../../models/znode';

interface Message {
  sender: string;
  text: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit {
  @Input({required: true}) userNodes!: ZNode[]; 
  
  messagesMap: { [key: string]: UserMessage[] } = {}; // Store messages per user
  messageContent: string = '';

  messages$ = this.messageService.messages$;

  username: string = this.authService.getUsername();
  users: string[] = []; 
  selectedUser = '';
  hoveredUser = '';
  globalMessage = "global";
  nodesCopy: ZNode[] | undefined;

  constructor(
    // private chatService: ChatService,
    private authService: AuthService,
    private messageService: MessagingService
  ) {}

  ngOnInit(): void {
    this.messageService.messages$.subscribe((messages) => {
     
      if(messages.length > 0) {
        const newMessage: UserMessage = messages.pop()!;

        if(newMessage.from === newMessage.to) {
          // Global message
          if (!this.messagesMap[this.globalMessage]) {
            this.messagesMap[this.globalMessage] = [];
          }

          this.messagesMap[this.globalMessage].push(newMessage);
        }
        else {
          // Private
          if(newMessage.from === this.username) {
            // It is my message or global message
            if(newMessage.to != this.username) {
              // It is my message with another user
              // Check if that map exists
              if (!this.messagesMap[newMessage.to]) {
                this.messagesMap[newMessage.to] = [];
              }
  
              this.messagesMap[newMessage.to].push(newMessage);
            }
          } 
          else {
            if (!this.messagesMap[newMessage.from]) {
              this.messagesMap[newMessage.from] = [];
            }
  
            this.messagesMap[newMessage.from].push(newMessage);
          }
        }
      }
    });
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.nodesCopy = JSON.parse(JSON.stringify(this.userNodes));
      this.updateUserName();
    }, 0);
  }
  
  updateUserName() {
    const foundIndex = this.nodesCopy!.findIndex(
      zNode => zNode.name === this.username
    );

    // Step 3: If found, update the name to "global"
    if (foundIndex !== -1) {
      this.nodesCopy![foundIndex].name = "global";
    }
    else {
      console.warn(`User with name "${this.username}" not found.`);
    }
  }

  sendMessage() {
    if (this.messageContent.trim()) {

      if(this.selectedUser != '') {
        const userMessage: UserMessage = {
          from: this.authService.getUsername(), 
          to: this.selectedUser == "global" ? this.authService.getUsername() : this.selectedUser, 
          text: this.messageContent, 
          read: false
        };
        
        this.messageService.sendMessage(userMessage).subscribe({
          next: (res: any) => {
            if(res) {
            }
          },
          error: (err: any) => {
            console.error(err);
          }
        })
        
        this.messageContent = '';
      }
    }
  }

  selectUser(user: string) {
    this.selectedUser = user;
  }

  hoverUser(user: string) {
    this.hoveredUser = user;
  }
}
