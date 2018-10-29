import { Component, OnInit } from '@angular/core';

import { OnlineUsersService } from '../services/online-users.service';
import { User } from '../shared/user';

@Component({
  selector: 'app-online-users',
  templateUrl: './online-users.component.html',
  styleUrls: ['./online-users.component.scss']
})
export class OnlineUsersComponent implements OnInit {

  users: User[];
  errMess: string;

  constructor(private ous: OnlineUsersService) { }

  ngOnInit() {

  	this.ous.getOnlineUsers()
  		.subscribe(leaders => this.users = leaders,
        errmess => this.errMess = <any>errmess.message);
  	
  }
}
