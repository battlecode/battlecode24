import * as cst from '../../constants'

import { schema } from 'battlecode-playback'
import Victor = require('victor')

import { MapUnit } from '../index'

export default class TileForm {

  // The public div
  readonly div: HTMLDivElement

  // Form elements
  readonly walls: HTMLInputElement
  readonly brush: HTMLInputElement
  readonly style: HTMLSelectElement

  // Callbacks on input change
  readonly width: () => number
  readonly height: () => number

  constructor(width: () => number, height: () => number) {

    // Store the callbacks
    this.width = width
    this.height = height

    // Create HTML elements
    this.div = document.createElement("div")

    this.walls = document.createElement("input")
    this.walls.type = "checkbox"
    this.walls.checked = true

    this.brush = document.createElement("input")
    this.brush.value = "1"

    this.style = document.createElement("select")
    for (var styleString of ["Circle", "Square", "Cow"]) {
      var option = document.createElement("option")
      option.value = styleString
      option.appendChild(document.createTextNode(styleString))
      this.style.appendChild(option)
    }

    this.div.appendChild(this.createForm())
    this.loadCallbacks()
  }



  /**
   * Creates the HTML form that collects archon information.
   */
  private createForm(): HTMLFormElement {
    // HTML structure
    const form: HTMLFormElement = document.createElement("form")
    form.id = "change-tiles"
    const pass: HTMLDivElement = document.createElement("div")
    const brush: HTMLDivElement = document.createElement("div")
    const style: HTMLDivElement = document.createElement("div")

    pass.appendChild(document.createTextNode("Walls:"))
    pass.appendChild(this.walls)
    form.appendChild(pass)

    brush.appendChild(document.createTextNode("Brush size:"))
    brush.appendChild(this.brush)
    form.appendChild(brush)

    style.appendChild(document.createTextNode("Brush style:"))
    style.appendChild(this.style)
    form.appendChild(style)

    form.appendChild(document.createElement("br"))


    return form
  }

  /**
   * Add callbacks to the form elements.
   */
  private loadCallbacks(): void {
    this.brush.onchange = () => {
      this.brush.value = !isNaN(this.getBrush()) ? this.validate(this.getBrush(), 1) : ""
    }
  }

  getWalls(): boolean {
    return this.walls.checked
  }

  getBrush(): number {
    return parseFloat(this.brush.value)
  }

  getStyle(): String {
    return this.style.value
  }

  resetForm(): void {
    this.walls.checked = true
    this.brush.value = "1"
  }

  setForm(): void {
  }

  private validate(value: number, min: number = 0, max: number = Infinity) {
    value = Math.max(value, min)
    value = Math.min(value, max)
    return isNaN(value) ? "" : String(value)
  }

  isValid(): boolean {
    return true
  }
}
