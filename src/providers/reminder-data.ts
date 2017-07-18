import { Injectable } from "@angular/core";

import { Observable } from "rxjs/Observable";
import "rxjs/add/observable/of";

import { Reminder, ReminderType } from "../models/index";

@Injectable()
export class ReminderData {


    getAll(): Observable<Reminder[]> {

        let reminders: Reminder[] = [
            {
                id: 1,
                description: "Boiler Service",
                icon: { ionicIcon: "flask" },
                reminderType: ReminderType.Yearly,
                nextDueOn: new Date(2018, 8, 8),
                notes: "Call the plumber",
                expectedCost: { value: 123.23, currency: "GBP", symbol: "£" }
            },
            {
                id: 2,
                description: "MOT",
                icon: { ionicIcon: "car" },
                reminderType: ReminderType.HalfYearly,
                nextDueOn: new Date(2018, 4, 8),
                notes: "Book appointment 4 weeks before",
                contact: {
                    businessAddress: {
                        address1: "1",
                        city: "Leeds",
                        country: "UK",
                        postcode: "ls27 9rw"
                    },
                    businessPhoneNumbers: ["12342342421"]
                },
                expectedCost: { value: 132.00, currency: "GBP", symbol: "£" }
            }
        ]

        return Observable.of(reminders);
    }

}