import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatListItemModule } from '@angular/material/list';

import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { HomeComponent } from './home/home.component';
import { FooterComponent } from './footer/footer.component';

import { AppRoutingModule } from './app-routing/app-routing.module';

import { OnlineUsersService } from './services/online-users.service';
import { OnlineUsersComponent } from './online-users/online-users.component';
import { RequestsComponent } from './requests/requests.component';

import { baseURL } from './shared/baseurl';
import { RestangularModule, Restangular } from 'ngx-restangular';
import { RestangularConfigFactory } from './shared/restConfig';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    HomeComponent,
    FooterComponent,
    OnlineUsersComponent,
    RequestsComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,

    MatToolbarModule,
    MatButtonModule,
    
    RestangularModule.forRoot(RestangularConfigFactory)
  ],
  providers: [
    OnlineUsersService,
    {provide: 'BaseURL', useValue: baseURL},
  ],
  entryComponents: [
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
