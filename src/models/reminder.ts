import { Money } from "./money";
import { ReminderType } from "./reminder-type";
import { Contact } from "./contact";
import { Icon } from "./icon";

export class Reminder {
    id: number;
    icon: Icon;
    description: string;
    reminderType: ReminderType;
    nextDueOn: Date;
    contact?: Contact;
    expectedCost?: Money;
    notes?: string;
    attachments?: Map<string, string>;

}