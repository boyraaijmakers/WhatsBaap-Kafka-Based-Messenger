import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

import { Restangular } from 'ngx-restangular';

import { User } from '../shared/user';

@Injectable({
  providedIn: 'root'
})
export class ManagerService {

  constructor(private restangular: Restangular) { }

  getRegisteredUsers(): Observable<User[]> {
    return this.restangular.all('registeredUsers').getList();
  }

  getRequests() {
  	this.restangular.all('getRequests').post();
  }

  handleEnroll(req): any {
  	this.restangular.all('registerUser').post(req);
  }

  handleQuit(req): any {
  	this.restangular.all('removeUser').post(req);
  }

  getManagementMode(): any {
    return this.restangular.all('managementMode').get();
  }

  setManagementMode(req): any {
    this.restangular.all('managementMode').post(req);
  }
}