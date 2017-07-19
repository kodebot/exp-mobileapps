import { Component } from "@angular/core";
import { NavController, NavParams } from "ionic-angular";

import { Observable } from "rxjs/Observable";

import { ReminderDetailPage } from "../reminder-detail/reminder-detail";

import { Reminder } from "../../models/index";
import { ReminderData } from "../../providers/reminder-data";


@Component({
  selector: "reminder-list-page",
  templateUrl: "reminder-list.html"
})
export class ReminderListPage {
  selectedItem: any;
  icons: string[];
  items: Observable<Reminder[]>;

  constructor(
    private _scheduleData: ReminderData,
    private _navCtrl: NavController,
    private _navParams: NavParams) {
    this.items = this._scheduleData.getAll();
  }

  itemTapped(event, item) {
    this._navCtrl.push(ReminderDetailPage, {
      item: item
    });
  }
}
