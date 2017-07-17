import { Money } from "./money";
import { ScheduleType } from "./schedule-type";
import { Contact } from "./contact";

export class Schedule {
    id: number;
    icon:string;
    description: string;
    scheduleType: ScheduleType;
    nextDueOn: Date;
    contact?: Contact;
    expectedCost?: Money;
    notes?: string;
    attachments?: Map<string, string>;

}