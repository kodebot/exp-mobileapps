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
    private scheduleData: ReminderData,
    public navCtrl: NavController,
    public navParams: NavParams) {
    // If we navigated to this page, we will have an item available as a nav param
    this.selectedItem = navParams.get("item");

    this.items = this.scheduleData.getAll();
  }

  itemTapped(event, item) {
    // That"s right, we"re pushing to ourselves!
    this.navCtrl.push(ReminderDetailPage, {
      item: item
    });
  }
}
