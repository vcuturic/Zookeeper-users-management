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
import * as ConnectionState from '../constants/rx-stomp-constants'
import { environment } from '../../environments/environment';
import { ConfigurationService } from '../services/configuration.service';
import { ChatComponent } from '../components/chat/chat.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NodeTreeComponent,
    NodesVisualComponent,
    ChatComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, OnDestroy{

  zNodes: ZNode[] = [];
  allNodesChildren: ZNode[] = [];
  receivedMessages: string[] = [];
  availableBackEndUrl?: string;
  lastAttemptedWebSocketUrl: string = environment.websocketUrl1;
  connectedToBackend: boolean = false;
  connectingToBackend: boolean = false;

  private topicSubscription?: Subscription;

  constructor(
    private zooKeeperService: ZookeeperService,
    private rxStompService: RxStompService,
    private configurationService: ConfigurationService
    ) {}

  ngOnInit(): void {
    
    this.rxStompService.onConnecting().subscribe((serverUrl: string) => {
      this.lastAttemptedWebSocketUrl = serverUrl;
      console.log(`Attempting to connect to WebSocket url: ${serverUrl}`);
    });

    this.rxStompService.connectionState$.subscribe((state: number) => {
      // console.log("Connection state:", state);
      if(state == ConnectionState.CONNECTING && !this.connectingToBackend && !this.connectedToBackend) {
        this.checkBackendAvailability(0);
        this.connectingToBackend = true;
      }
      if(state == ConnectionState.CLOSED)
        this.connectedToBackend = false;

      if(this.lastAttemptedWebSocketUrl && (state == ConnectionState.CLOSED || state == ConnectionState.OPEN)) {
        const portNumber = this.lastAttemptedWebSocketUrl.split(":")[2].split("/")[0];
        const processedZNode = this.allNodesChildren.find(zNode => zNode.name.includes(portNumber));

        if(processedZNode) {
          const newMessage: MessageStructure = {
            operation: "",
            zNode: processedZNode
          }

          if(state == ConnectionState.CLOSED) {
            newMessage.operation = Constants.OPERATION_DELETE;
          }
          else if(state == ConnectionState.OPEN) {
            if(this.allNodesChildren.length == 0)
              newMessage.operation = Constants.OPERATION_CONNECT;
            else 
              newMessage.operation = Constants.OPERATION_RECONNECT;
          }

          console.log(newMessage);
          this.handleBackendMessages(JSON.stringify(newMessage));
        }
      }
    });

    this.topicSubscription = this.rxStompService
      .watch(Constants.DESTINATION_ROUTE)
      .subscribe((message: Message) => {
        this.handleBackendMessages(message.body);
    });
  }

  async checkBackendAvailability(currentBackendIndex: number) {
    const backendUrls = [`${environment.backEndUrl1}`, `${environment.backEndUrl2}`, `${environment.backEndUrl3}`];
    console.log("Attempting to connect to Backend url: " + backendUrls[currentBackendIndex]);
    await new Promise(resolve => setTimeout(resolve, 3000));

    this.configurationService.checkForAvailableBackendUrl(backendUrls[currentBackendIndex]).subscribe({
      next: (res: any) => {
        if(res) {
          this.availableBackEndUrl = backendUrls[currentBackendIndex];
          console.log("Successfully connected to Backend url: " + backendUrls[currentBackendIndex]);
          this.getAllZnodesAndChildren();
          this.getAllNodesChildren();
          this.connectedToBackend = true;
          this.connectingToBackend = false;
        }
      },
      error: (err: any) => {
        if(currentBackendIndex+1 < backendUrls.length) {
          console.log("Failed to connect to Backend url: " + backendUrls[currentBackendIndex]);
          this.checkBackendAvailability(++currentBackendIndex);
        }
        else {
          console.warn(err);
          this.checkBackendAvailability(0);
        }
      }
    });
  }

  handleBackendMessages(jsonString: string) {
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

  ngOnDestroy(): void {
    if(this.topicSubscription)
      this.topicSubscription.unsubscribe();
  }

  sendMessage() {
    const message = `Message generated at ${new Date()}`;
    this.rxStompService.publish({destination: '/app/hello', body: message})
  }

  getAllNodesChildren() {
    this.zooKeeperService.getAllNodesChildren(this.availableBackEndUrl!).subscribe({
      next: (res: any) => {
        if(res) {
          if(this.allNodesChildren)
            this.allNodesChildren = [];
          for (let i = 0; i < res.length; i++) {
            const newNode: ZNode = {
              name: res[i],
              children: [],
              online: false
            }
            this.allNodesChildren.push(newNode);
          }

          this.getLiveNodesChildren();
        }
      },
      error: (err: any) => {
        this.checkBackendAvailability(0);
      }
    });
  }

  getLiveNodesChildren() {
    this.zooKeeperService.getLiveNodesChildren(this.availableBackEndUrl!).subscribe({
      next: (res: any) => {
        if(res) {
          if(this.allNodesChildren) {
            for (let i = 0; i < res.length; i++) {
              const foundIndex = this.allNodesChildren.findIndex(zNode => zNode.name === res[i]);
              if(foundIndex != -1)
                this.allNodesChildren[foundIndex].online = true;
            }
          }
        }
      },
      error: (err: any) => {
        this.checkBackendAvailability(0);
      }
    });
  }

  getAllZnodesAndChildren() {
    this.zooKeeperService.getAllZnodesWithChildren(this.availableBackEndUrl!).subscribe({
      next: (res: any) => {
        if(res) {
          const list = res[0].children;

          for (let i = 0; i < list.length; i++) {
            this.zNodes.push(list[i]);
          }

        }
      },
      error: (err: any) => {
        this.checkBackendAvailability(0);
      }
    });
  }

}
