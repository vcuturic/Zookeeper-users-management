import { Injectable, OnDestroy } from '@angular/core';
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
export class BackendService implements OnDestroy {
  availableBackEndUrl?: string;
  lastAttemptedWebSocketUrl: string = environment.websocketUrl1;
  connectedToBackend: boolean = false;
  connectingToBackend: boolean = false;
  failMessageSendToBackendInfo: boolean = false;
  // WebSocket connects on the next instance, while backend instance connects from the first
  // and they load until both are connected to the same instance. This index will act as same
  // as websocket url
  currentBackendIndex: number = 0; 

  private updateMessagesFromBackendSubject = new BehaviorSubject<string>('');
  updateMessagesFromBackend$ = this.updateMessagesFromBackendSubject.asObservable();

  private connectingToBackendSubject = new BehaviorSubject<boolean>(false);
  connectingToBackend$ = this.connectingToBackendSubject.asObservable();
  
  allNodesChildren: ZNode[] = [];
  private topicSubscription?: Subscription;
  
  constructor(
    private rxStompService: RxStompService,
    private configurationService: ConfigurationService,
    private backendInfoService: BackendInfoService,
    private zooKeeperService: ZookeeperService,
  ) { }

  ngOnDestroy(): void {
    this.destruct();
  }

  setConnectingStatus(newValue: boolean) {
    this.connectingToBackendSubject.next(newValue);
  }

  getNextMessage() {
    console.log(this.connectingToBackendSubject.getValue());
  }

  init() {
    this.rxStompService.onConnecting().subscribe((serverUrl: string) => {
      this.lastAttemptedWebSocketUrl = serverUrl;
      console.log(`Attempting to connect to WebSocket url: ${serverUrl}`);
    });

    this.rxStompService.connectionState$.subscribe((state: number) => {
      if(state == ConnectionState.CONNECTING && !this.connectingToBackend && !this.connectedToBackend) {
        if(this.currentBackendIndex >= 3)
          this.currentBackendIndex = 0;
        this.checkBackendAvailability(this.currentBackendIndex++);
        this.connectingToBackend = true;
        this.setConnectingStatus(true);
      }
      if(state == ConnectionState.CLOSED)
        this.connectedToBackend = false;

      // if(this.lastAttemptedWebSocketUrl && (state == ConnectionState.CLOSED || state == ConnectionState.OPEN)) {
      //   const portNumber = this.lastAttemptedWebSocketUrl.split(":")[2].split("/")[0];
      //   const processedZNode = this.allNodesChildren.find(zNode => zNode.name.includes(portNumber));

        // if(processedZNode) {
        //   const newMessage: MessageStructure = {
        //     operation: "",
        //     zNode: processedZNode
        //   }

        //   if(state == ConnectionState.CLOSED) {
        //     newMessage.operation = Constants.OPERATION_DELETE;
        //   }
        //   else if(state == ConnectionState.OPEN) {
        //     if(this.allNodesChildren.length == 0)
        //       newMessage.operation = Constants.OPERATION_CONNECT;
        //     else 
        //       newMessage.operation = Constants.OPERATION_RECONNECT;
        //   }

        //   this.storeBackendMessages(JSON.stringify(newMessage));
        // }
      // }
    });

    // if(!this.topicSubscription) {
    //   this.topicSubscription = this.rxStompService
    //     .watch(Constants.DESTINATION_ROUTE)
    //     .subscribe((message: Message) => {
    //       this.storeBackendMessages(message.body);
    //   });
    // }
  }

  destruct() {
    if(this.topicSubscription)
      this.topicSubscription.unsubscribe();
  }

  getBackendTopicSubscription(): boolean {
    return this.topicSubscription ? true : false;
  }

  async checkBackendAvailability(currentBackendIndex: number) {
    if(!this.connectedToBackend) {
      const backendUrls = [`${environment.backEndUrl1}`, `${environment.backEndUrl2}`, `${environment.backEndUrl3}`];

      // console.log("Attempting to connect to Backend url: " + backendUrls[currentBackendIndex]);
      console.log("Attempting to connect to Backend url: " + this.getBackendUrlByPort());
      
      await new Promise(resolve => setTimeout(resolve, 2000));

      this.configurationService.checkForAvailableBackendUrl(this.getBackendUrlByPort()).subscribe({
        next: (res: any) => {
          if(res) {
            this.availableBackEndUrl = this.getBackendUrlByPort();
            console.log("Successfully connected to Backend url: " + this.getBackendUrlByPort());
            this.backendInfoService.setBackendUrl(this.getBackendUrlByPort());
            this.connectedToBackend = true;
            this.connectingToBackend = false;
            this.setConnectingStatus(false);
            this.failMessageSendToBackendInfo = false;
          }
        },
        error: (err: any) => {
          if(currentBackendIndex+1 < backendUrls.length) {
            console.log("Failed to connect to Backend url: " + this.getBackendUrlByPort());
            this.checkBackendAvailability(++currentBackendIndex);
          }
          else {
            console.warn(err);
            this.checkBackendAvailability(0);
          }
        }
      });
    }
  }

  storeBackendMessages(jsonString: string) {
    // console.log("storeBackendMessages(): " + jsonString);
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
          for (let i = 0; i < res.length; i++) {
            const newNode: ZNode = {
              name: res[i].name,
              children: [],
              online: false,
              type: res[i].type
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

  getBackendUrlByPort(): string {
    const currentPort = window.location.port;

    if(currentPort === "4200")
      return environment.backEndUrl1;
    if(currentPort === "4201")
      return environment.backEndUrl2;
    if(currentPort === "4202")
      return environment.backEndUrl3;

    return "error";
  }

  getAllZnodesAndChildren(zNodes: ZNode[], backendUrl?: string) {
    var backendInitialized = false;

    if (backendUrl) {
      backendInitialized = true;
    } 

    this.zooKeeperService.getAllZnodesWithChildren(backendInitialized ? backendUrl! : this.availableBackEndUrl!).subscribe({
      next: (res: any) => {
        if(res) {

          if(zNodes.length > 0) 
            zNodes.length = 0;
          
          const list = res[0].children;

          for (let i = 0; i < list.length; i++) {
            zNodes.push(list[i]);
          }

          // console.log(zNodes);

        }

        return zNodes;
      },
      error: (err: any) => {
        this.checkBackendAvailability(0);
      }
    });
  }
}
