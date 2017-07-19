import { Component } from "@angular/core";

import { Reminder, ReminderType } from "../../models/index";

@Component({
    selector: "reminder-detail-page",
    templateUrl: "reminder-detail.html"
})
export class ReminderDetailPage {

    reminder: Reminder;

    constructor() {
        this.reminder = {
            id: 1,
            name: "MOT",
            nextDueOn: new Date(2010, 10, 10),
            reminderType: ReminderType.Daily,
            icon: { ionicIcon: 'car' }
        };
    }

}