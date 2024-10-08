import { Component, OnDestroy, OnInit } from '@angular/core';
import { NodeTreeComponent } from '../components/node-tree/node-tree.component';
import { NodesVisualComponent } from '../components/nodes-visual/nodes-visual.component';
import { ZNode } from '../models/znode';
import { RxStompService } from '../services/rx-stomp.service';
import { Message } from '@stomp/stompjs';
import { Subscription } from 'rxjs';
import { MessageStructure } from '../models/message-structure';
import * as Constants from '../constants/constants';
import { ChatComponent } from '../components/chat/chat-component/chat.component';
import { BackendInfoService } from '../services/backend-info.service';
import { BackendService } from '../services/backend.service';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';
import { ButtonModule } from 'primeng/button';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LoadingComponent } from '../components/loading/loading.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NodeTreeComponent,
    NodesVisualComponent,
    ChatComponent,
    ButtonModule,
    CommonModule,
    LoadingComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, OnDestroy{

  zNodes: ZNode[] = [];
  allNodesChildren: ZNode[] = [];
  personNodes: ZNode[] = [];
  private topicSubscription?: Subscription;
  private connectingToBackendSubscription: Subscription;
  private subscription?: Subscription;
  private updateMessagesFromBackend: string = '';
  initTriggered: boolean = false;
  loading: boolean = true;
  backendUrl: string = '';

  constructor(
    private rxStompService: RxStompService,
    private backendInfoService: BackendInfoService,
    private backendService: BackendService,
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    ) {
        this.connectingToBackendSubscription = this.backendService.connectingToBackend$.subscribe(value => {
          if(value) {
            this.loading = true;
            this.allNodesChildren = [];
            this.zNodes = []; 
            // this.topicSubscription?.unsubscribe(); 
            // this.backendService.destruct(); 
          }
        });
    }

  ngOnInit(): void {
    this.backendInfoService.getBackendUrl().subscribe((url) => {
      if (url === 'none' && !this.initTriggered) {
        this.backendService.init();
        this.initTriggered = true;
      } else {
        // Handle the case when backendUrl is updated
        console.log('Backend URL updated: ', url, this.initTriggered);
        this.backendUrl = url;

        this.userService.sendHeartbeat();

        this.backendService.getAllZnodesAndChildren(this.zNodes, url);
        // this.backendService.getAllNodesChildren(this.allNodesChildren, url);
        this.getAllPersonNodes();


        // setTimeout(() => {
          // console.log('This runs after the current execution context');
          // this.backendService.getLiveNodesChildren(this.allNodesChildren, url);
        // }, 0);
        
        if(!this.topicSubscription) {
          this.topicSubscription = this.rxStompService
            .watch(Constants.DESTINATION_ROUTE)
            .subscribe((message: Message) => {
              this.handleBackendMessages(message.body);
          }); 
        }
         

        // if(this.initTriggered) {
        //   if(this.backendService.getBackendTopicSubscription()) {
        //     this.backendService.destruct();
        //     console.log("Ladies and gentleman, we got them.");
        //   }

        //   this.topicSubscription = this.rxStompService
        //     .watch(Constants.DESTINATION_ROUTE)
        //     .subscribe((message: Message) => {
        //       this.handleBackendMessages(message.body);
        //   });     
        // } 
        // else {
        //   // Why double CONNECT_OFFLINE
        //   // Scenario:
        //   // 1# Login, initTriggered == true. Some1 added an user, u handle it (line 90), but the BE-Service stores it
        //   // 2# Logout
        //   // 3# Login, initTriggered == false. Now u read that stored message (line 101)
        //   this.subscription = this.backendService.updateMessagesFromBackend$.subscribe(value => {
        //     this.updateMessagesFromBackend = value;
        //     this.handleBackendMessages(value);
        //   });
        // }

        this.loading = false;
      }
    });  
  }

  getAllPersonNodes() {
    this.userService.getUsers().subscribe({
      next: (res: any) => {
        if(res) {
          if(this.personNodes.length > 0) 
            this.personNodes.length = 0;

          res.forEach((element: any) => {
            const newNode: ZNode = {
              name: element.username,
              online: element.online,
              type: Constants.ZNODE_TYPE_USER
            };

            this.personNodes.push(newNode);
          });
        }
      },
      error: (err: any) => {
        console.error(err);
      }
    });
  }

  unsubscribeFromBackendMessages() {
    if(this.initTriggered) {
      // After Login
      this.topicSubscription?.unsubscribe();
    }
    else {
      // No Login
      if(this.subscription)
        this.subscription.unsubscribe();
    }

    this.backendService.destruct(); // Must, because login.component always initializes new backend
  }

  ngOnDestroy(): void {
    this.unsubscribeFromBackendMessages();
  }

  private handleBackendMessages(jsonString: string) {
    if(jsonString) {
      const msg: MessageStructure = JSON.parse(jsonString);
      console.log(msg);

      if(msg.zNode.name === this.authService.getUsername()) {
        if(msg.operation === Constants.OPERATION_DISCONNECT) {
          msg.operation = Constants.OPERATION_ERROR;
          console.log("ERROR happend: " + msg.operation);
          this.backendService.getNextMessage();
        }
      }

      if(msg.operation === Constants.OPERATION_CONNECT_OFFLINE) {
          msg.zNode.online = false;
          // this.allNodesChildren.push(msg.zNode);
          this.personNodes.push(msg.zNode);
      }
      if(msg.operation === Constants.OPERATION_CONNECT_ONLINE) {
        msg.zNode.online = true;
        // this.allNodesChildren.push(msg.zNode);
        this.personNodes.push(msg.zNode);
      }
      if(msg.operation === Constants.OPERATION_RECONNECT) {
        // const foundIndex = this.allNodesChildren.findIndex(zNode => zNode.name === msg.zNode.name);
        const foundIndex = this.personNodes.findIndex(zNode => zNode.name === msg.zNode.name);

        if(foundIndex !== -1) {
          // this.allNodesChildren[foundIndex].online = true;
          this.personNodes[foundIndex].online = true;
        }
      }
      if(msg.operation === Constants.OPERATION_DISCONNECT) {
        // const foundIndex = this.allNodesChildren.findIndex(zNode => zNode.name === msg.zNode.name);
        const foundIndex = this.personNodes.findIndex(zNode => zNode.name === msg.zNode.name);

        if(foundIndex !== -1) {
          // this.allNodesChildren[foundIndex].online = false;
          this.personNodes[foundIndex].online = false;
        }
      }
      if(msg.operation === Constants.OPERATION_DELETE) {
        // const foundIndex = this.allNodesChildren.findIndex(zNode => zNode.name === msg.zNode.name);
        const foundIndex = this.personNodes.findIndex(zNode => zNode.name === msg.zNode.name);

        if(foundIndex !== -1) {
          // this.allNodesChildren = this.allNodesChildren.filter(znode => znode.name !== msg.zNode.name);
          this.personNodes = this.personNodes.filter(znode => znode.name !== msg.zNode.name);
        }
      }

      this.backendService.getAllZnodesAndChildren(this.zNodes, this.backendUrl);
    }
  }

  logout() {
    this.authService.logout().subscribe({
      next: (res: any) => { 
        if(res) {
          console.log(res);
          this.userService.stopHeartbeat();
          // this.backendService.clearUpMessagesFromUser();
          this.unsubscribeFromBackendMessages();
          // this.backendService.destruct();
          this.authService.deleteCookies();
        }
      },
      error: (err: any) => {
        console.error(err);
      },
      complete: () => {
        this.router.navigate(['/login']);
      }
    });
  }

  onAllNodesChange(updatedZNodes: ZNode[]) {
    this.personNodes = updatedZNodes;
  }
}
