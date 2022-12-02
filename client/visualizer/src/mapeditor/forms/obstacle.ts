import * as cst from '../../constants'

import { schema } from 'battlecode-playback'

import { MapUnit } from '../index'

export default class ObstacleForm {

  // The public div
  readonly div: HTMLDivElement

  readonly cloud: HTMLInputElement
  readonly current: HTMLInputElement
  readonly current_dir: HTMLInputElement
  readonly none: HTMLInputElement

  // Callbacks on input change
  readonly width: () => number
  readonly height: () => number

  constructor(width: () => number, height: () => number) {

    // Store the callbacks
    this.width = width
    this.height = height

    // Create HTML elements
    this.div = document.createElement("div")
    this.cloud = document.createElement("input")
    this.current = document.createElement("input")
    this.none = document.createElement("input")
    this.current_dir = document.createElement("input")
    this.cloud.type = "radio"
    this.cloud.name = "obstacle_type"
    this.cloud.checked = true
    this.none.type = "radio"
    this.none.name = "obstacle_type"
    this.none.checked = false
    this.current.type = "radio"
    this.current.name = "obstacle_type"
    this.current_dir.type = "number"
    this.current_dir.value = "1"
    this.current_dir.min = "1"
    this.current_dir.max = "8"
    // Create the form
    this.div.appendChild(this.createForm())
    this.loadCallbacks()
  }

  // /**
  //  * Creates the HTML form that collects archon information.
  //  */
  private createForm(): HTMLFormElement {
    // HTML structure
    const form: HTMLFormElement = document.createElement("form")
    form.onkeydown = (event) => event.key != "Enter"
    const resources: HTMLDivElement = document.createElement("div")
    // const influence: HTMLDivElement = document.createElement("div");
    //form.appendChild(id);
    form.appendChild(resources)
    // form.appendChild(influence);
    form.appendChild(document.createElement("br"))

    resources.appendChild(document.createTextNode("Cloud: "))
    resources.appendChild(this.cloud)
    resources.appendChild(document.createElement("br"))
    resources.appendChild(document.createTextNode("Current: "))
    resources.appendChild(this.current)
    resources.appendChild(this.current_dir)
    resources.appendChild(document.createElement("br"))
    resources.appendChild(document.createTextNode("None: "))
    resources.appendChild(this.none)

    // Influence
    // influence.appendChild(document.createTextNode("I: "));
    // influence.appendChild(this.influence);

    return form
  }

  // /**
  //  * Add callbacks to the form elements.
  //  */
  private loadCallbacks(): void {

    // // X must be in the range [0, this.width]
    // this.x.onchange = () => {
    //   let value: number = this.getX()
    //   value = Math.max(value, 0)
    //   value = Math.min(value, this.width())
    //   this.x.value = isNaN(value) ? "" : String(value)
    // }

    // // Y must be in the range [0, this.height]
    // this.y.onchange = () => {
    //   let value: number = this.getY()
    //   value = Math.max(value, 0)
    //   value = Math.min(value, this.height())
    //   this.y.value = isNaN(value) ? "" : String(value)
    // }

    this.current_dir.onchange = () => {
      if(parseInt(this.current_dir.value) == NaN || this.current_dir.value.includes('.'))
        this.current_dir.value = "1"
      if(parseInt(this.current_dir.value) > 8)
        this.current_dir.value = "8"
      if(parseInt(this.current_dir.value) < 1)
        this.current_dir.value = "1"
    }
  }

  getObstacles(): { cloud: boolean, current: number } | null {
    if (this.none.checked) {
      return null
    }
    return {
      cloud: this.cloud.checked,
      current: this.current.checked ? parseInt(this.current_dir.value) : 0
    }
  }

  // getX(): number {
  //   return parseInt(this.x.value)
  // }

  // getY(): number {
  //   return parseInt(this.y.value)
  // }

  resetForm(): void {
    // this.x.value = ""
    // this.y.value = ""
    this.cloud.checked = true
    this.current_dir.value = "1"
  }

  setForm(x, y): void {

  }

  // isValid(): boolean {
  //   const x = this.getX()
  //   const y = this.getY()
  //   //const I = this.getInfluence();
  //   return !(isNaN(x) || isNaN(y)) // || isNaN(I));
  // }
}
