import { Injectable } from "@angular/core";

import { Observable } from "rxjs/Observable";
import "rxjs/add/observable/of";

import { Schedule, ScheduleType } from "../models/index";

@Injectable()
export class ScheduleData {


    getAll(): Observable<Schedule[]> {

        let schedules: Schedule[] = [
            {
                id: 1,
                description: "Boiler Service",
                icon:"flask",
                scheduleType: ScheduleType.Yearly,
                nextDueOn: new Date(2018, 8, 8),
                notes: "Call the plumber",
                expectedCost: { value: 123.23, currency: "GBP", symbol: "£" }
            },
            {
                id: 2,
                description: "MOT",
                icon:"car",
                scheduleType: ScheduleType.Yearly,
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

        return Observable.of(schedules);
    }

}