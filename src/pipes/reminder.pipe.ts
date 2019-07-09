import { Pipe, PipeTransform } from "@angular/core";

import { ReminderType } from "../models/index";

@Pipe({ name: "reminder" })
export class ReminderPipe implements PipeTransform {
    transform(input: number): string {
        switch (input) {
            case ReminderType.Daily:
                return "daily";
            case ReminderType.Weekly:
                return "weekly";
            case ReminderType.Fortnightly:
                return "fortnightly";
            case ReminderType.Monthly:
                return "monthly";
            case ReminderType.Quarterly:
                return "quarterly";
            case ReminderType.HalfYearly:
                return "half-yearly";
            case ReminderType.Yearly:
                return "yearly";
            case ReminderType.Other:
                return "other";
        }
    }
}