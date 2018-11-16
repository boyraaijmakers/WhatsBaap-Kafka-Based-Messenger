import { Component, OnInit } from '@angular/core';

import { ManagerService } from '../services/manager.service';
import { User } from '../shared/user';

import { MatTableDataSource } from '@angular/material';

@Component({
  selector: 'app-online-users',
  templateUrl: './online-users.component.html',
  styleUrls: ['./online-users.component.scss']
})
export class OnlineUsersComponent implements OnInit {

  users: User[];
  errMess: string;

  displayedColumns = ['name', 'status'];
  dataSource;

  constructor(private ous: ManagerService) { }

  ngOnInit() {

  	this.ous.getRegisteredUsers()
  		.subscribe(users => {
        this.users = users;
        this.dataSource = new MatTableDataSource(this.users);
      }, errmess => this.errMess = <any>errmess.message);
  }
}
