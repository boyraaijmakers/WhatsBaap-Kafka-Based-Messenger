import { Routes } from '@angular/router';

import { HomeComponent } from '../home/home.component';
import { OnlineUsersComponent } from '../online-users/online-users.component';
import { RequestsComponent } from '../requests/requests.component'; 

export const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'users', component: OnlineUsersComponent },
  { path: 'requests', component: RequestsComponent },
  { path: '', redirectTo: '/home', pathMatch: 'full' }
];