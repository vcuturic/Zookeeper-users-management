import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ZNode } from '../../models/znode';
import { ButtonModule } from 'primeng/button';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PasswordModule } from 'primeng/password';
import { LoginData } from '../../models/login-data';
import { AuthService } from '../../services/auth.service';
import * as Constants from '../../constants/constants';
// PRIMENG
import { MenuItem } from 'primeng/api';
import { MenuModule } from 'primeng/menu';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { InputTextModule } from 'primeng/inputtext';
import { SplitButtonModule } from 'primeng/splitbutton';

@Component({
  selector: 'app-nodes-visual',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    FormsModule, 
    InputGroupModule, 
    InputGroupAddonModule, 
    InputTextModule,
    SplitButtonModule,
    PasswordModule,
    ReactiveFormsModule,
    MenuModule
  ],
  templateUrl: './nodes-visual.component.html',
  styleUrl: './nodes-visual.component.css'
})
export class NodesVisualComponent implements OnInit {
  @Input({required: true}) zNodes!: ZNode[]; 

  items: MenuItem[];
  itemsShown: boolean = false;
  hidePassword = true;
  userOptionsItems: MenuItem[] = [];
  selectedNodeForRemoval?: ZNode;
  
  addUserForm = new FormGroup({
    username: new FormControl('', ),
    password: new FormControl('', )
  });

  constructor(
    private authService: AuthService,
    private renderer: Renderer2
  ) {
    this.items = [
      {
          label: 'Show fields',
          command: () => {
              this.showFields();
          }
      },
      {
          label: 'Hide fields',
          command: () => {
              this.hideFields();
          }
      }
    ];
  }

  ngOnInit(): void {
    this.itemsShown = true;
  }
  
  

  addUser() {
    const loginData: LoginData = this.addUserForm.value as LoginData;
    
    console.log(loginData);

    this.authService.login(loginData, true).subscribe({
      next: (res: any) => {
        if(res) {
          console.log(res);
          this.itemsShown = false;
        }
      },
      error: (err: any) => {
        console.error(err);
      }
    });
  }

  removeUser(zNode: ZNode) {
    // TODO, nmg vishe hahahah :)
    console.log("removeUser()");
  }

  showFields() {
    this.itemsShown = true;
  }

  handleZNodeClick(zNode: ZNode, event: MouseEvent) {
    if(zNode.type == Constants.ZNODE_TYPE_USER) {
      this.populateMenu(zNode);
      this.closeMenus(zNode);
      
      zNode.showMenu = !zNode.showMenu;

      setTimeout(() => {
        const absoluteElement = document.getElementById('absoluteElement' + zNode.name);
        if (absoluteElement) {
          const height = absoluteElement.offsetHeight;
          this.renderer.setStyle(absoluteElement, 'bottom', `-${height}px`);
          this.renderer.setStyle(absoluteElement, 'visibility', 'visible');
        }
      }, 0); 

      event.stopPropagation();
    }
  }

  populateMenu(zNode: ZNode) {
    if(this.userOptionsItems.length > 0)
        this.userOptionsItems = [];

    this.userOptionsItems.push({label: 'Remove user ' + zNode.name,
                                icon: 'pi pi-user-minus',
                                command: () => {
                                  this.removeUser(zNode);
                                }});
  }

  closeMenus(expect?: ZNode): void {
    if(expect) {
      this.zNodes.forEach(znode => {
        if (znode.name !== expect.name) {
          znode.showMenu = false;
        }
      });
    }
    else {
      this.zNodes.forEach(znode => znode.showMenu = false);
    }
  }

  hideFields() {
    this.itemsShown = false;
  }
}
