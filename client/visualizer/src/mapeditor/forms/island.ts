import * as cst from '../../constants'

import { schema } from 'battlecode-playback'

import { MapUnit } from '../index'

export default class IslandForm {

  // The public div
  readonly div: HTMLDivElement

  readonly island_add: HTMLInputElement
  // readonly x: HTMLInputElement
  // readonly y: HTMLInputElement

  // Callbacks on input change
  readonly width: () => number
  readonly height: () => number

  constructor(width: () => number, height: () => number) {

    // Store the callbacks
    this.width = width
    this.height = height

    // Create HTML elements
    this.div = document.createElement("div")
    this.island_add = document.createElement("input")
    this.island_add.type = "checkbox"
    this.island_add.checked = true
    // this.x = document.createElement("input")
    // this.y = document.createElement("input")
    // this.x.type = "text";
    // this.y.type = "text";
    this.div.appendChild(this.createForm())
    // this.loadCallbacks()
  }

  // /**
  //  * Initializes input fields.
  //  */
  // private loadInputs(): void {
  //   this.x.type = "text";
  //   this.y.type = "text";
  //   this.adamantium.checked = true;
  // }

  /**
   * Creates the HTML form that collects archon information.
   */
  private createForm(): HTMLFormElement {
    // HTML structure
    const form: HTMLFormElement = document.createElement("form");
    const x: HTMLDivElement = document.createElement("div");
    const y: HTMLDivElement = document.createElement("div");
    // const influence: HTMLDivElement = document.createElement("div");
    //form.appendChild(id);
    // form.appendChild(x);
    // form.appendChild(y);
    // form.appendChild(influence);
    form.appendChild(document.createTextNode("Island: "))
    form.appendChild(this.island_add);
    // X coordinate
    // x.appendChild(document.createTextNode("X: "));
    // x.appendChild(this.x);

    // // Y coordinate
    // y.appendChild(document.createTextNode("Y: "));
    // y.appendChild(this.y);
    
    return form;
  }

  /**
   * Add callbacks to the form elements.
  //  */
  // private loadCallbacks(): void {

  //   // X must be in the range [0, this.width]
  //   this.x.onchange = () => {
  //     let value: number = this.getX();
  //     value = Math.max(value, 0);
  //     value = Math.min(value, this.width());
  //     this.x.value = isNaN(value) ? "" : String(value);
  //   };

  //   // Y must be in the range [0, this.height]
  //   this.y.onchange = () => {
  //     let value: number = this.getY();
  //     value = Math.max(value, 0);
  //     value = Math.min(value, this.height());
  //     this.y.value = isNaN(value) ? "" : String(value);
  //   };

  // }

  addIsland(): boolean {
    return this.island_add.checked;
  }

  resetForm(): void {
    // this.x.value = "";
    // this.y.value = "";
    this.island_add.checked = true;
  }

  setForm(x, y): void {

  }

  // isValid(): boolean {
  //   const x = this.getX();
  //   const y = this.getY();
  //   //const I = this.getInfluence();
  //   return !(isNaN(x) || isNaN(y)); // || isNaN(I));
  // }
}
