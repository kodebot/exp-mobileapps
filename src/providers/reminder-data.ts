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
                name: "Boiler Service",
                icon: { ionicIcon: "flask" },
                reminderType: ReminderType.Yearly,
                nextDueOn: new Date(2018, 8, 8),
                notes: "Call the plumber",
                expectedCost: { value: 123.23, currency: "GBP", symbol: "£" }
            },
            {
                id: 2,
                name: "MOT",
                icon: { ionicIcon: "car" },
                reminderType: ReminderType.Fortnightly,
                nextDueOn: new Date(2018, 4, 8),
                notes: "Book appointment 4 weeks before",
                contact: {
                    businessAddress: {
                        address1: "1",
                        city: "Leeds",
                        country: "UK",
                        postcode: "ls27 9rw"
                    },
                    personalAddress:{
                        address1:"2",
                        address2:"Victoria Grange Drive",
                        address3: "Morley",
                        address4: "address line 4",
                        city:"Leeds",
                        postcode:"LS27 9RW"
                    },
                    businessPhoneNumbers: ["12342342421","32432432432"],
                    personalPhoneNumbers: ["123432432432","32432432432"],
                    personalEmails:["mail@mail.com"],
                    businessEmails: ["anothermail@mail.com"],
                },
                expectedCost: { value: 132.00, currency: "GBP", symbol: "£" }
            }
        ]

        return Observable.of(reminders);
    }

}