import { BrowserModule } from "@angular/platform-browser";
import { ErrorHandler, NgModule } from "@angular/core";
import { IonicApp, IonicErrorHandler, IonicModule } from "ionic-angular";

import { StatusBar } from "@ionic-native/status-bar";
import { SplashScreen } from "@ionic-native/splash-screen";

import { YearlyApp } from "./app.component";
import { HomePage } from "../pages/home/home";
import { ReminderListPage } from "../pages/reminder-list/reminder-list";

import {ReminderData} from "../providers/reminder-data";

@NgModule({
  declarations: [
    YearlyApp,
    HomePage,
    ReminderListPage
  ],
  imports: [
    BrowserModule,
    IonicModule.forRoot(YearlyApp),
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    YearlyApp,
    HomePage,
    ReminderListPage
  ],
  providers: [
    StatusBar,
    SplashScreen,
    ReminderData,
    {provide: ErrorHandler, useClass: IonicErrorHandler}
  ]
})
export class AppModule {}
