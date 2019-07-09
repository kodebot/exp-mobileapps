import {Address} from "./address";

export class Contact{
    personalEmails?:string[];
    businessEmails?:string[];
    personalPhoneNumbers?:string[];
    businessPhoneNumbers?:string[];
    personalAddress?:Address;
    businessAddress?:Address;
    other?:Map<string, string[] | Address>;
}