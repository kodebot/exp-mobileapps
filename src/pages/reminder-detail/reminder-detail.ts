import { Component } from "@angular/core";

import { NavController, NavParams } from "ionic-angular";

import { Reminder, ReminderType } from "../../models/index";

@Component({
    selector: "reminder-detail-page",
    templateUrl: "reminder-detail.html"
})
export class ReminderDetailPage {

    reminder: Reminder;

    constructor(
        private _navController: NavController,
        private _navParams: NavParams) {
        this.reminder = this._navParams.get("item");
    }

}