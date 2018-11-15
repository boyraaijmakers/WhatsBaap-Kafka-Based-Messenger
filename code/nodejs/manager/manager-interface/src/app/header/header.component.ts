import { Component, OnInit } from '@angular/core';

import { ManagerService } from '../services/manager.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

	managementMode: boolean = false;
	errMess: string;

  constructor(private ous: ManagerService) { }

  ngOnInit() {
  	this.ous.getManagementMode()
  		.subscribe(managementMode => {
        this.managementMode = managementMode;
      }, errmess => this.errMess = <any>errmess.message);
  }

  changeManagementMode() {
    this.managementMode = !this.managementMode;
    console.log(this.managementMode);
  	this.ous.setManagementMode({
  		mode: this.managementMode
  	});
  }

}
