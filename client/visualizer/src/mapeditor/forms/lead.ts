import * as cst from '../../constants';

import {schema} from 'battlecode-playback';

import {MapUnit} from '../index';

export default class RobotForm {

  // The public div
  readonly div: HTMLDivElement;

  // Form elements for archon settings
  readonly lead: HTMLInputElement;
  readonly x: HTMLInputElement;
  readonly y: HTMLInputElement;
  //readonly influence: HTMLInputElement;

  // Callbacks on input change
  readonly width: () => number;
  readonly height: () => number;

  constructor(width: () => number, height: () => number) {

    // Store the callbacks
    this.width = width;
    this.height = height;

    // Create HTML elements
    this.div = document.createElement("div");
    this.lead = document.createElement("input");
    this.x = document.createElement("input");
    this.y = document.createElement("input");

    // Create the form
    this.loadInputs();
    this.div.appendChild(this.createForm());
    this.loadCallbacks();
  }

  /**
   * Initializes input fields.
   */
  private loadInputs(): void {
    this.x.type = "text";
    this.y.type = "text";
    this.lead.value = "50";
  }

  /**
   * Creates the HTML form that collects archon information.
   */
  private createForm(): HTMLFormElement {
    // HTML structure
    const form: HTMLFormElement = document.createElement("form");
    const lead: HTMLDivElement = document.createElement("div");
    const x: HTMLDivElement = document.createElement("div");
    const y: HTMLDivElement = document.createElement("div");
    // const influence: HTMLDivElement = document.createElement("div");
    //form.appendChild(id);
    form.appendChild(lead);
    form.appendChild(x);
    form.appendChild(y);
    // form.appendChild(influence);
    form.appendChild(document.createElement("br"));

    lead.appendChild(document.createTextNode("Lead: "));
    lead.appendChild(this.lead);

    // X coordinate
    x.appendChild(document.createTextNode("X: "));
    x.appendChild(this.x);

    // Y coordinate
    y.appendChild(document.createTextNode("Y: "));
    y.appendChild(this.y);

    // Influence
    // influence.appendChild(document.createTextNode("I: "));
    // influence.appendChild(this.influence);

    return form;
  }

  /**
   * Add callbacks to the form elements.
   */
  private loadCallbacks(): void {

    // X must be in the range [0, this.width]
    this.x.onchange = () => {
      let value: number = this.getX();
      value = Math.max(value, 0);
      value = Math.min(value, this.width());
      this.x.value = isNaN(value) ? "" : String(value);
    };

    // Y must be in the range [0, this.height]
    this.y.onchange = () => {
      let value: number = this.getY();
      value = Math.max(value, 0);
      value = Math.min(value, this.height());
      this.y.value = isNaN(value) ? "" : String(value);
    };

    // this.influence.onchange = () => {
    //   let value: number = this.getInfluence();
    //   value = Math.max(value, 50);
    //   value = Math.min(value, 500);
    //   this.influence.value = isNaN(value) ? "" : String(value);
    // }

    // this.team.onchange = () => {
    //   if (this.getTeam() !== 0) {
    //     this.influence.disabled = true;
    //     this.influence.value = String(cst.INITIAL_INFLUENCE);
    //   }
    //   else this.influence.disabled = false;
    // }

  }

  private getLead(): number {
    return parseInt(this.lead.value);
  }

  private getX(): number {
    return parseInt(this.x.value);
  }

  private getY(): number {
    return parseInt(this.y.value);
  }

  resetForm(): void {
    this.x.value = "";
    this.y.value = "";
    this.lead.value = "50";
  }

  setForm(x, y): void {
    this.x.value = String(x);
    this.y.value = String(y);
  }

  isValid(): boolean {
    const x = this.getX();
    const y = this.getY();
    //const I = this.getInfluence();
    return !(isNaN(x) || isNaN(y)); // || isNaN(I));
  }
}
