import * as cst from '../../constants';

import {schema} from 'battlecode-playback';

import {MapUnit} from '../index';

export default class AnomalyForm {

  // The public div
  readonly div: HTMLDivElement;

  // Form elements for archon settings
  readonly anomaly: HTMLSelectElement;
  readonly round: HTMLInputElement;

  private readonly ANOMALIES = cst.anomalyList;
  
  constructor() {

    // Create HTML elements
    this.div = document.createElement("div");
    this.anomaly = document.createElement("select");
    this.round = document.createElement("input");

    // Create the form
    this.loadInputs();
    this.div.appendChild(this.createForm());
    this.loadCallbacks();
  }

  /**
   * Initializes input fields.
   */
  private loadInputs(): void {
    this.round.type = "text";
    this.ANOMALIES.forEach((anomaly: schema.Action) => {
      const option = document.createElement("option");
      option.value = String(anomaly);
      option.appendChild(document.createTextNode(cst.anomalyToString(anomaly)));
      this.anomaly.appendChild(option);
    });
  }

  /**
   * Creates the HTML form that collects archon information.
   */
  private createForm(): HTMLFormElement {
    // HTML structure
    const form: HTMLFormElement = document.createElement("form");
    const anomaly: HTMLDivElement = document.createElement("div");
    const round: HTMLDivElement = document.createElement("div");
    // const influence: HTMLDivElement = document.createElement("div");
    //form.appendChild(id);
    form.appendChild(anomaly);
    form.appendChild(round);
    form.appendChild(document.createElement("br"));

    // Robot type
    anomaly.appendChild(document.createTextNode("Anomaly: "));
    anomaly.appendChild(this.anomaly);

    // X coordinate
    round.appendChild(document.createTextNode("Round: "));
    round.appendChild(this.round);

    return form;
  }

  /**
   * Add callbacks to the form elements.
   */
  private loadCallbacks(): void {

    this.round.onchange = () => {
      let value: number = this.getRound();
      value = Math.max(value, 0);
      value = Math.min(value, 2000); // TODO: don't hard code
      this.round.value = isNaN(value) ? "" : String(value);
    };

  }

  getAnomaly(): schema.Action {
    return parseInt(this.anomaly.options[this.anomaly.selectedIndex].value);
  }

  getRound(): number {
    return parseInt(this.round.value);
  }

  setForm() : void {
    return;
  }

  resetForm(): void {
    this.anomaly.value = ""; // TODO
    this.round.value = "100";
  }

  isValid(): boolean {
    const round = this.getRound();
    //const I = this.getInfluence();
    return !isNaN(round);
  }
}
