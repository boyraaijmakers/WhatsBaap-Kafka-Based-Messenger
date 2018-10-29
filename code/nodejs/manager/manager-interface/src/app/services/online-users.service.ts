import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

import { Restangular } from 'ngx-restangular';

import { User } from '../shared/user';

@Injectable({
  providedIn: 'root'
})
export class OnlineUsersService {

  constructor(private restangular: Restangular) { }

  getOnlineUsers(): Observable<User[]> {
    return this.restangular.all('onlineUsers').getList();
  }
}