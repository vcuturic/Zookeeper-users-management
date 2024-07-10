import { Injectable } from '@angular/core';
//
import { RxStompService } from './rx-stomp.service';
import { environment } from '../../environments/environment';
import * as ConnectionState from '../constants/rx-stomp-constants'
import * as Constants from '../constants/constants';
import { ZNode } from '../models/znode';
import { MessageStructure } from '../models/message-structure';
import { BehaviorSubject, Subscription } from 'rxjs';
import { Message } from '@stomp/stompjs';
import { ConfigurationService } from './configuration.service';
import { BackendInfoService } from './backend-info.service';
import { ZookeeperService } from './zookeeper.service';

@Injectable({
  providedIn: 'root'
})
export class BackendService {
  availableBackEndUrl?: string;
  lastAttemptedWebSocketUrl: string = environment.websocketUrl1;
  connectedToBackend: boolean = false;
  connectingToBackend: boolean = false;

  private updateMessagesFromBackendSubject = new BehaviorSubject<string>('');
  updateMessagesFromBackend$ = this.updateMessagesFromBackendSubject.asObservable();
  
  allNodesChildren: ZNode[] = [];
  private topicSubscription?: Subscription;
  
  constructor(
    private rxStompService: RxStompService,
    private configurationService: ConfigurationService,
    private backendInfoService: BackendInfoService,
    private zooKeeperService: ZookeeperService,
  ) { }

  init() {
    this.rxStompService.onConnecting().subscribe((serverUrl: string) => {
      this.lastAttemptedWebSocketUrl = serverUrl;
      console.log(`Attempting to connect to WebSocket url: ${serverUrl}`);
    });

    this.rxStompService.connectionState$.subscribe((state: number) => {
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

          this.storeBackendMessages(JSON.stringify(newMessage));
        }
      }
    });

    this.topicSubscription = this.rxStompService
      .watch(Constants.DESTINATION_ROUTE)
      .subscribe((message: Message) => {
        this.storeBackendMessages(message.body);
    });
  }

  destruct() {
    if(this.topicSubscription)
      this.topicSubscription.unsubscribe();
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
          this.backendInfoService.setBackendUrl(backendUrls[currentBackendIndex]);
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

  storeBackendMessages(jsonString: string) {
    this.updateMessagesFromBackendSubject.next(jsonString);
  }

  getAllNodesChildren(allNodesChildren: ZNode[], backendUrl?: string) {
    var backendInitialized = false;

    if (backendUrl) {
      backendInitialized = true;
    } 

    this.zooKeeperService.getAllNodesChildren(backendInitialized ? backendUrl! : this.availableBackEndUrl!).subscribe({
      next: (res: any) => {
        if(res) {
          // if(allNodesChildren2)
            // allNodesChildren2 = [];
          
          for (let i = 0; i < res.length; i++) {
            const newNode: ZNode = {
              name: res[i],
              children: [],
              online: false
            }
            allNodesChildren.push(newNode);
          }
        }
      },
      error: (err: any) => {
        this.checkBackendAvailability(0);
      }
    });
  }

  getLiveNodesChildren(allNodesChildren: ZNode[], backendUrl?: string) {
    var backendInitialized = false;

    if (backendUrl) {
      backendInitialized = true;
    } 

    this.zooKeeperService.getLiveNodesChildren(backendInitialized ? backendUrl! : this.availableBackEndUrl!).subscribe({
      next: (res: any) => {
        if(res) {
          if(allNodesChildren) {
            for (let i = 0; i < res.length; i++) {
              const foundIndex = allNodesChildren.findIndex(zNode => zNode.name === res[i]);
              if(foundIndex != -1)
                allNodesChildren[foundIndex].online = true;
            }
          }
        }
      },
      error: (err: any) => {
        this.checkBackendAvailability(0);
      }
    });
  }

  getAllZnodesAndChildren(zNodes: ZNode[], backendUrl?: string) {
    var backendInitialized = false;

    if (backendUrl) {
      backendInitialized = true;
    } 

    this.zooKeeperService.getAllZnodesWithChildren(backendInitialized ? backendUrl! : this.availableBackEndUrl!).subscribe({
      next: (res: any) => {
        if(res) {
          const list = res[0].children;

          for (let i = 0; i < list.length; i++) {
            zNodes.push(list[i]);
          }
        }
      },
      error: (err: any) => {
        this.checkBackendAvailability(0);
      }
    });
  }
}
