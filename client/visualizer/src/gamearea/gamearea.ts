import { Config, Mode } from '../config'
import { AllImages } from '../imageloader'
import Client from '../app'

import { GameWorld } from 'battlecode-playback'

import { http } from '../main/electron-modules'
import { SSL_OP_NO_QUERY_MTU } from 'constants'

export enum CanvasType {
  BACKGROUND = 0,
  DYNAMIC = 1,
  OVERLAY = 2
}

export default class GameArea {

  // HTML elements
  private readonly images: AllImages
  readonly div: HTMLDivElement
  readonly canvases: Record<CanvasType, HTMLCanvasElement>
  readonly splashDiv: HTMLDivElement
  private readonly wrapper: HTMLDivElement
  private readonly mapEditorCanvas: HTMLCanvasElement
  private readonly profilerIFrame?: HTMLIFrameElement

  // Options
  private readonly conf: Config

  constructor(conf: Config, images: AllImages, mapEditorCanvas: HTMLCanvasElement, profilerIFrame?: HTMLIFrameElement) {
    this.div = document.createElement("div")
    this.div.id = "gamearea"
    this.conf = conf
    this.images = images
    this.mapEditorCanvas = mapEditorCanvas
    this.canvases = {} as Record<CanvasType, HTMLCanvasElement>
    this.profilerIFrame = profilerIFrame

    // Create the canvas
    const wrapper: HTMLDivElement = document.createElement("div")
    wrapper.id = "canvas-wrapper"
    this.wrapper = wrapper

    for (const type in [CanvasType.BACKGROUND, CanvasType.DYNAMIC, CanvasType.OVERLAY]) {
      const canvas = document.createElement("canvas")
      canvas.className = "game-canvas"
      this.canvases[type] = canvas
    }

    this.splashDiv = document.createElement("div")
    this.splashDiv.id = "battlecode-splash"
    this.loadSplashDiv()

    // Add elements to the main div
    this.div.appendChild(this.wrapper)
  }

  /**
   * Sets canvas size to maximum dimensions while maintaining the aspect ratio
   */
  setCanvasDimensions(world: GameWorld): void {
    const width = world.minCorner.absDistanceX(world.maxCorner)
    const height = world.minCorner.absDistanceY(world.maxCorner)
    const scale = this.conf.upscale / Math.sqrt(width * height)
    for (let key in this.canvases) {
      const canvas = this.canvases[key]
      if (!this.conf.doingRotate) {
        canvas.width = width * scale
        canvas.height = height * scale
      }
      else {
        canvas.width = height * scale
        canvas.height = width * scale
      }
    }
  }

  /**
   * Displays the splash screen
   */
  loadSplashDiv() {

    let splashTitle = document.createElement("h1")
    let splashSubtitle = document.createElement("h3")
    splashTitle.id = "splashTitle"
    splashSubtitle.id = "splashSubtitle"

    if (!this.conf.tournamentMode) {
      splashTitle.appendChild(document.createTextNode("Battlecode " + this.conf.year + " Client"))
      splashSubtitle.appendChild(document.createTextNode("v" + this.conf.gameVersion))
    }
    else {
      splashTitle.appendChild(document.createTextNode("Loading..."))
    }

    this.splashDiv.appendChild(splashTitle)
    this.splashDiv.appendChild(splashSubtitle)

    let inst = this
    if (process.env.ELECTRON) {
      (async function (splashDiv, version) {

        var options = {
          host: inst.conf.year + '.battlecode.org',
          path: '/versions/' + inst.conf.year + '/version.txt'
        }

        var req = http.get(options, function (res) {
          let data = ""
          res.on('data', function (chunk) {
            data += chunk
          }).on('end', function () {

            var latest = data

            if (latest.trim() != version.trim()) {
              let newVersion = document.createElement("p")
              newVersion.id = "splashNewVersion"
              newVersion.innerHTML = "New version available (download with <code>gradle update</code> followed by <code>gradle build</code>, and then restart the client): v" + latest
              splashDiv.appendChild(newVersion)
            }

          })
        })
      })(inst.splashDiv, inst.conf.gameVersion)
    }
  }

  /**
   * Displays the correct canvas depending on whether we are in game mode
   * or map editor mode
   */
  setCanvas() {
    var mode = this.conf.mode
    // haHAA 
    var splash = this.conf.splash && mode !== Mode.MAPEDITOR && mode !== Mode.PROFILER

    // The canvas can be anything in help mode
    if (mode === Mode.HELP) return

    // Otherwise clear the canvas area...
    while (this.wrapper.firstChild) {
      this.wrapper.removeChild(this.wrapper.firstChild)
    }
    this.splashDiv.remove()

    // ...and add the correct one
    var shouldListen = true
    if (splash) {
      shouldListen = false
      this.div.appendChild(this.splashDiv)

      // Reset change listeners
      window.onresize = function () { }
    } else {
      switch (mode) {
        case Mode.MAPEDITOR:
          this.wrapper.appendChild(this.mapEditorCanvas)
          break
        case Mode.PROFILER:
          if (this.profilerIFrame) this.wrapper.appendChild(this.profilerIFrame)
          break
        default:
          for (let key in this.canvases)
            this.wrapper.appendChild(this.canvases[key]) // TODO: Only append if a game is available in client.games
          console.log("Running a game")
      }
    }

    if (shouldListen) {
      window.onresize = function () {
        var wrapper = <HTMLDivElement>document.getElementById("canvas-wrapper")
        var splash = <HTMLDivElement>document.getElementById("battlecode-splash")
        if (wrapper.firstChild && splash) {
          var currentCanvas = <HTMLCanvasElement>wrapper.firstChild

          // This part is nasty, but handles the case where no game is in the canvas
          // If the map dimensions just so happen to equal these parameters, this will
          // still have the desired effect, as the client does not show with a map of
          // that size
          if (currentCanvas.clientHeight != 150 && currentCanvas.clientWidth != 300) {
            splash.style.maxHeight = "" + currentCanvas.clientHeight + "px"
            splash.style.maxWidth = "" + currentCanvas.clientWidth + "px"
          } else {
            splash.style.maxHeight = ""
            splash.style.maxWidth = ""
          }
        }
      }
    }

    // Reset splash size and reconfigure
    this.splashDiv.style.maxHeight = ""
    this.splashDiv.style.maxWidth = ""
    window.dispatchEvent(new Event('resize'))
  };
}
