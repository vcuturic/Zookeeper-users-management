import { Component, OnDestroy, OnInit } from '@angular/core';
import { NodeTreeComponent } from '../components/node-tree/node-tree.component';
import { NodesVisualComponent } from '../components/nodes-visual/nodes-visual.component';
import { ZNode } from '../models/znode';
import { ZookeeperService } from '../services/zookeeper.service';
import { RxStompService } from '../services/rx-stomp.service';
import { Message } from '@stomp/stompjs';
import { Subscription } from 'rxjs';
import { MessageStructure } from '../models/message-structure';
import * as Constants from '../constants/constants';

import { environment } from '../../environments/environment';
import { ConfigurationService } from '../services/configuration.service';
import { ChatComponent } from '../components/chat/chat-component/chat.component';
import { UserListComponent } from '../components/chat/user-list/user-list.component';
import { BackendInfoService } from '../services/backend-info.service';
import { BackendService } from '../services/backend.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NodeTreeComponent,
    NodesVisualComponent,
    ChatComponent,
    UserListComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, OnDestroy{

  zNodes: ZNode[] = [];
  allNodesChildren: ZNode[] = [];
  private topicSubscription?: Subscription;
  private subscription: Subscription;
  private updateMessagesFromBackend: string = ''
  initTriggered: boolean = false;

  constructor(
    private backendService: BackendService,
    private backendInfoService: BackendInfoService,
    private rxStompService: RxStompService,
    ) {
      // if some backend instance failed durning login page, we need to read it and fix it
      this.subscription = this.backendService.updateMessagesFromBackend$.subscribe(value => {
        this.updateMessagesFromBackend = value;
        this.handleBackendMessages(value);
      });
    }
  

  private handleBackendMessages(jsonString: string) {
    if(jsonString) {
      const msg: MessageStructure = JSON.parse(jsonString);

      if(msg.operation === Constants.OPERATION_CONNECT) {
        if(!this.allNodesChildren.find(obj => obj.name === msg.zNode.name)) {
          this.allNodesChildren.push(msg.zNode);
        }
      }
      else {
        const foundIndex = this.allNodesChildren.findIndex(zNode => zNode.name === msg.zNode.name);

        if(msg.operation === Constants.OPERATION_DELETE) {
          if(foundIndex !== -1) {
            this.allNodesChildren[foundIndex].online = false;

            if(this.allNodesChildren.every(zNode => zNode.online == false)) {
              // last node disconnected - remove all_nodes
              this.allNodesChildren = [];
            }
          }
        }
        else if(msg.operation === Constants.OPERATION_RECONNECT) {
          if(foundIndex !== -1) {
            this.allNodesChildren[foundIndex].online = true;
          }
        }
      }
    }
  }

  ngOnInit(): void {
    this.backendInfoService.getBackendUrl().subscribe((url) => {
      if (url === 'none') {
        this.backendService.init();
        this.initTriggered = true;
      } else {
        // Handle the case when backendUrl is updated
        console.log('Backend URL updated: ', url, this.initTriggered);
        this.backendService.getAllZnodesAndChildren(this.zNodes, url);
        this.backendService.getAllNodesChildren(this.allNodesChildren, url);
        this.backendService.getLiveNodesChildren(this.allNodesChildren, url);

        // Subscription resets when components change, so we need to refresh subscription
        if(!this.initTriggered) {
          this.topicSubscription = this.rxStompService
            .watch(Constants.DESTINATION_ROUTE)
            .subscribe((message: Message) => {
              this.handleBackendMessages(message.body);
          });
        }
      }
    });  
  }

  ngOnDestroy(): void {
    this.backendService.destruct();
  }
}
