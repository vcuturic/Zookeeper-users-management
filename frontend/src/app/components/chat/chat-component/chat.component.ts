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
export class ChatComponent {
  @Input({required: true}) userNodes!: ZNode[]; 
  
  messageContent: string = '';
  messages$ = this.messageService.messages$;
  username: string = this.authService.getUsername();
  users: string[] = []; 
  selectedUser = '';
  hoveredUser = '';

  constructor(
    // private chatService: ChatService,
    private authService: AuthService,
    private messageService: MessagingService
  ) {}

  sendMessage() {
    if (this.messageContent.trim()) {
      // this.chatService.sendMessage(this.messageContent);
      const userMessage: UserMessage = {from: this.authService.getUsername(), to: this.selectedUser, text: this.messageContent};
      
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

  selectUser(user: string) {
    this.selectedUser = user;
    console.log('Selected user:', user);
  }

  hoverUser(user: string) {
    this.hoveredUser = user;
  }
}
