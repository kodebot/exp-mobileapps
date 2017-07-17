import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';

import {Observable} from "rxjs/Observable";

import {Schedule} from "../../models/index";
import {ScheduleData} from "../../providers/schedule-data";


@Component({
  selector: 'schedule-list-page',
  templateUrl: 'schedule-list.html'
})
export class ScheduleListPage {
  selectedItem: any;
  icons: string[];
  items: Observable<Schedule[]>;

  constructor(
    private scheduleData:ScheduleData,
    public navCtrl: NavController, 
    public navParams: NavParams) {
    // If we navigated to this page, we will have an item available as a nav param
    this.selectedItem = navParams.get('item');

    this.items = this.scheduleData.getAll();
  }

  itemTapped(event, item) {
    // That's right, we're pushing to ourselves!
    this.navCtrl.push(ScheduleListPage, {
      item: item
    });
  }
}
