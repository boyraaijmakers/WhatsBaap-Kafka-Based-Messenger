import { Component, OnInit } from '@angular/core';
import { PusherService } from '../services/pusher.service';
import { ManagerService } from '../services/manager.service';

@Component({
  selector: 'app-requests',
  templateUrl: './requests.component.html',
  styleUrls: ['./requests.component.scss']
})
export class RequestsComponent implements OnInit {

	title = 'Pending Request';
  pendingEnroll: any;
  pendingQuit: any;


  constructor(private pusherService: PusherService,
              private managerService: ManagerService) { }

  ngOnInit() {
    this.pusherService.channel.bind('requests', data => {
      this.pendingEnroll = data["/request/enroll"];
      this.pendingQuit = data["/request/quit"];
    });

    this.managerService.getRequests();
  }

  approve(action: string, user: string) {
    if(action == "enroll") {
      this.managerService.handleEnroll({
        name: user,
        state: 1
      });
    } else if (action == "quit" ) {
      this.managerService.handleQuit({
        name: user,
        state: 1
      });
    }

    console.log(this.pendingEnroll);
    console.log(this.pendingQuit);
  }

  deny(action: string, user: string) {
    if(action == "enroll") {
      this.managerService.handleEnroll({
        name: user,
        state: 0
      });
    } else if (action == "quit" ) {
      this.managerService.handleQuit({
        name: user,
        state: 0
      });
    }

    console.log(this.pendingEnroll);
    console.log(this.pendingQuit);
  }
}
