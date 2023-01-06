import { Config, Mode } from '../config'
import * as imageloader from '../imageloader'
import * as cst from '../constants'
import { Game, Match, Metadata, schema } from 'battlecode-playback'
import Runner from '../runner'
import { BodyTypeMetaData } from 'battlecode-playback/out/metadata'

type ButtonInfo = {
  img: HTMLImageElement,
  text: string,
  onclick: () => void,
  changeTo?: string
}

export enum ControlType {
  GO_NEXT = 0,
  GO_PREVIOUS = 1,
  PLAYBACK_PAUSE = 2,
  PLAYBACK_START = 3,
  PLAYBACK_STOP = 4,
  // MATCH_FORWARD = 5,
  // MATCH_BACKWARD = 6,
  REVERSE_UPS = 7,
  DOUBLE_UPS = 8,
  HALVE_UPS = 9,
  GO_END = 10
};

/**
 * Game controls: pause/unpause, fast forward, rewind
 */
export default class Controls {
  div: HTMLDivElement
  wrapper: HTMLDivElement

  readonly timeReadout: HTMLSpanElement
  readonly speedReadout: HTMLSpanElement
  readonly tileInfo: HTMLSpanElement
  readonly infoString: HTMLTableDataCellElement

  //winnerDiv: HTMLDivElement;

  /**
   * Callbacks initialized from outside Controls
   * Most of these are defined in ../app.ts
   * This is for easily modifing local variables of app.ts, such as goalUPS.
   */

  // qualities of progress bar
  canvas: HTMLCanvasElement
  maxFrame: number
  ctx
  curUPS: number

  // buttons
  readonly conf: Config

  private runner: Runner

  readonly buttons: {
    playbackStart: ButtonInfo,
    playbackPause: ButtonInfo,
    playbackStop: ButtonInfo,
    goNext: ButtonInfo,
    goPrevious: ButtonInfo,
    reverseUPS: ButtonInfo,
    doubleUPS: ButtonInfo,
    halveUPS: ButtonInfo,
    goEnd: ButtonInfo
  }

  constructor(conf: Config, images: imageloader.AllImages, runner: Runner) {
    this.div = this.baseDiv()
    this.timeReadout = document.createElement('span')
    this.tileInfo = document.createElement('span')
    this.speedReadout = document.createElement('span')
    this.speedReadout.style.cssFloat = 'right'

    this.setDefaultText()

    // initialize the images
    this.conf = conf
    const imgs = images.controls
    this.buttons = {
      playbackStart: { img: imgs[ControlType.PLAYBACK_START], text: "Start", onclick: () => this.pause(), changeTo: 'playbackPause' },
      playbackPause: { img: imgs[ControlType.PLAYBACK_PAUSE], text: "Pause", onclick: () => this.pause(), changeTo: 'playbackStart' },
      playbackStop: { img: imgs[ControlType.PLAYBACK_STOP], text: "Stop", onclick: () => this.stop() },
      goNext: { img: imgs[ControlType.GO_NEXT], text: "Next", onclick: () => this.stepForward() },
      goPrevious: { img: imgs[ControlType.GO_PREVIOUS], text: "Prev", onclick: () => this.stepBackward() },
      reverseUPS: { img: imgs[ControlType.REVERSE_UPS], text: "Reverse", onclick: () => this.reverseUPS() },
      doubleUPS: { img: imgs[ControlType.DOUBLE_UPS], text: "Faster", onclick: () => this.doubleUPS() },
      halveUPS: { img: imgs[ControlType.HALVE_UPS], text: "Slower", onclick: () => this.halveUPS() },
      goEnd: { img: imgs[ControlType.GO_END], text: "End", onclick: () => this.end() }
    }
    this.runner = runner

    let table = document.createElement("table")
    let tr = document.createElement("tr")

    // create the timeline
    let timeline = document.createElement("td")
    timeline.className = "timeline"
    timeline.vAlign = "top"

    timeline.appendChild(this.timeline())
    timeline.appendChild(document.createElement("br"))
    timeline.appendChild(this.timeReadout)
    timeline.appendChild(this.speedReadout)

    this.setDefaultUPS()

    // create the button controls
    let buttons = document.createElement("td")
    buttons.vAlign = "top"

    let reverseButton = this.createButton('reverseUPS')

    let halveButton = this.createButton('halveUPS')
    let goPreviousButton = this.createButton('goPrevious')
    let pauseStartButton = this.createButton('playbackStart')
    let goNextButton = this.createButton('goNext')
    let doubleButton = this.createButton('doubleUPS')

    let stopButton = this.createButton('playbackStop')
    let endButton = this.createButton('goEnd')

    buttons.appendChild(reverseButton)
    buttons.appendChild(halveButton)
    buttons.appendChild(goPreviousButton)
    buttons.appendChild(pauseStartButton)
    buttons.appendChild(goNextButton)
    buttons.appendChild(doubleButton)
    buttons.appendChild(stopButton)
    buttons.appendChild(endButton)
    buttons.appendChild(document.createElement("br"))
    buttons.appendChild(this.tileInfo)

    pauseStartButton.title = "Pause/resume"
    stopButton.title = "Stop (go to start)"
    goPreviousButton.title = "Step back"
    goNextButton.title = "Step forward"
    doubleButton.title = "Double Speed"
    halveButton.title = "Halve Speed"
    reverseButton.title = "Play Reverse"
    endButton.title = "Go to end"

    // create the info string display
    let infoString = document.createElement("td")
    infoString.vAlign = "middle"
    infoString.className = "info"
    this.infoString = infoString

    table.appendChild(tr)
    tr.appendChild(timeline)
    tr.appendChild(document.createElement('div'))
    tr.appendChild(buttons)
    tr.appendChild(infoString)

    this.wrapper = document.createElement("div")
    this.wrapper.appendChild(table)
    this.div.appendChild(this.wrapper)

    function changeTime(dragEvent: MouseEvent) {
      // jump to a frame when clicking the controls timeline
      if (runner.looper) {
        const loadedTime = !conf.tournamentMode ? runner.looper.match['_farthest'].turn : 1500
        let width: number = (<HTMLCanvasElement>this).width
        let turn: number = dragEvent.offsetX / width * loadedTime
        turn = Math.round(Math.min(loadedTime, turn))

        runner.looper.onSeek(turn)
      }
    }
    this.canvas.addEventListener('click', changeTime)
    this.canvas.onmousedown = function (mousedownevent) {
      this.addEventListener('mousemove', changeTime)
    }
    this.canvas.onmouseup = function (mouseupevent) {
      this.removeEventListener('mousemove', changeTime)
    }
  }

  /**
   * @param content name of the image in this.imgs to display in the button
   * @param onclick function to call on click
   * @param hiddenContent name of the image in this.imgs to display as none
   * @return a button with the given attributes
   */
  private createButton(buttonId: string) {
    let button = document.createElement("button")
    button.setAttribute("class", "custom-button control-button")
    button.setAttribute("type", "button")
    button.id = buttonId

    const info = this.buttons[buttonId]

    // button.innerText = info.text;
    button.appendChild(info.img)

    const changeTo = info.changeTo
    if (changeTo != null) {
      let hiddenImage = this.buttons[changeTo].img
      hiddenImage.style.display = "none"
      button.appendChild(hiddenImage)
    }
    button.onclick = info.onclick

    return button
  }

  /**
   * Make the controls look good
   */
  private baseDiv() {
    let div = document.createElement("div")
    div.id = "baseDiv"

    return div
  }

  private timeline() {
    let canvas = document.createElement("canvas")
    canvas.id = "timelineCanvas"
    canvas.width = 300
    canvas.height = 1
    this.ctx = canvas.getContext("2d")
    this.ctx.fillStyle = "white"
    this.canvas = canvas
    if (this.conf.tournamentMode) {
      //canvas.style.display = 'none'; // we don't wanna reveal how many rounds there are!
    }
    return canvas
  }

  setDefaultText() {
    this.timeReadout.innerHTML = 'No match loaded'
    this.tileInfo.innerHTML = 'X | Y | Well'
    this.speedReadout.textContent = 'UPS:  FPS: '
  }

  /**
   * Returns the UPS determined by the slider
   */
  getUPS(): number {
    return this.curUPS
  }

  setDefaultUPS() {
    this.curUPS = 16
    if (this.conf.tournamentMode) {
      this.curUPS = 8 // for tournament!!!
    }
  }

  /**
   * Displays the correct controls depending on whether we are in game mode
   * or map editor mode
   */
  setControls = () => {
    const mode = this.conf.mode

    // The controls can be anything in help mode
    if (mode === Mode.HELP) return

    // Otherwise clear the controls...
    while (this.div.firstChild) {
      this.div.removeChild(this.div.firstChild)
    }

    // ...and add the correct thing
    if (mode !== Mode.MAPEDITOR) {
      this.div.appendChild(this.wrapper)
    }
  };

  /**
   * Upload a battlecode match file.
   */
  loadMatch(files: FileList) {
    const file = files[0]
    const reader = new FileReader()
    reader.onload = () => {
      this.runner.onGameLoaded(<ArrayBuffer>reader.result)
    }
    reader.readAsArrayBuffer(file)
  }

  /**
   * Toggle between pausing and playing our simulation.
   */
  pause() {
    if (this.runner.looper) {
      this.runner.looper.onTogglePause()
      this.updatePlayPauseButton(this.runner.looper.isPaused())
    }
  }
  /**
   * Update play/pause button.
   */
  updatePlayPauseButton(isPaused) {
    // toggle the play/pause button
    if (isPaused) {
      this.buttons["playbackStart"].img.style.display = "unset"
      this.buttons["playbackPause"].img.style.display = "none"
    } else {
      this.buttons["playbackStart"].img.style.display = "none"
      this.buttons["playbackPause"].img.style.display = "unset"
    }

  }

  /**
   * Stop the match, and go to the first round
   */
  stop() {
    if (this.runner.looper) this.runner.looper.onStop()
  }

  end() {
    if (this.runner.looper) this.runner.looper.onGoEnd()
  }

  /**
   * Steps forward one turn in the simulation
   */
  stepForward() {
    if (this.runner.looper) this.runner.looper.onStepForward()
  }

  /**
   * Steps backward one turn in the simulation
   */
  stepBackward() {
    if (this.runner.looper) this.runner.looper.onStepBackward()
  }

  /**
   * Doubles UPS (Max 128)
   */
  doubleUPS() {
    if (Math.abs(this.curUPS) < 128) {
      this.curUPS = this.curUPS * 2
    }
    if (this.runner.looper) this.runner.looper.onToggleUPS()
  }

  /**
   * Halves UPS (Min 1)
   */
  halveUPS() {
    if (Math.abs(this.curUPS) > 1) {
      this.curUPS = this.curUPS / 2
    }
    if (this.runner.looper) this.runner.looper.onToggleUPS()
  }

  /**
   * Changes the sign of UPS
   */
  reverseUPS() {
    this.curUPS = - this.curUPS
    if (this.runner.looper) this.runner.looper.onToggleUPS()
  }

  /**
   * When the match is finished, set UPS to 0.
   */
  onFinish(match: Match, meta: Metadata) {
    if (this.runner.looper && !this.runner.looper.isPaused()) this.pause()
    // if (this.conf.tournamentMode) {
    //   // also update the winner text
    //   this.setWinner(match, meta);
    // }
  }

  // setWinner(match: Match, meta: Metadata) {
  //   console.log('winner: ' + match.winner);
  //   const matchWinner = this.winnerTeam(meta.teams, match.winner);
  //   while (this.winnerDiv.firstChild) {
  //     this.winnerDiv.removeChild(this.winnerDiv.firstChild);
  //   }
  //   this.winnerDiv.appendChild(matchWinner);
  // }

  // private winnerTeam(teams, winnerID: number | null): HTMLSpanElement {
  //   const span = document.createElement("span");
  //   if (winnerID === null) {
  //     return span;
  //   } else {
  //     // Find the winner
  //     let teamNumber = 1;
  //     for (let team in teams) {
  //       if (teams[team].teamID === winnerID) {
  //         span.className += team === "1" ? " red" : " blue";
  //         span.innerHTML = teams[team].name + " wins!";
  //         break;
  //       }
  //     }
  //   }
  //   return span;
  // }

  /**
   * Redraws the timeline and sets the current round displayed in the controls.
   */
  // TODO scale should be constant; should not depend on loadedTime
  setTime(time: number, loadedTime: number, upsUnpaused: number, paused: Boolean, fps: number, lagging: Boolean, maxTurn: number) {

    if (this.conf.tournamentMode) loadedTime = maxTurn

    // if (!this.conf.tournamentMode) {
    // Redraw the timeline

    const scale = this.canvas.width / loadedTime
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)

    this.ctx.fillStyle = cst.UI_GREEN
    this.ctx.fillRect(0, 0, time * scale, this.canvas.height)

    this.ctx.fillStyle = cst.UI_GREY
    this.ctx.fillRect(time * scale, 0, (loadedTime - time) * scale, this.canvas.height)

    this.ctx.fillStyle = cst.UI_GREEN_DARK
    this.ctx.fillRect(time * scale, 0, 2, this.canvas.height)
    // }

    let speedText = (lagging ? '(Lagging) ' : '') + `UPS: ${upsUnpaused | 0}` + (paused ? ' (Paused)' : '') + ` FPS: ${fps | 0}`
    speedText = speedText.padStart(32)
    this.speedReadout.textContent = speedText
    this.timeReadout.innerHTML = (this.conf.tournamentMode ? `Round: <b>${time}</b>` : `Round: <b>${time}</b>/${loadedTime}`)

  }

  /**
   * Updates the location readout
   */
  setTileInfo(x: number, y: number, xrel: number, yrel: number, resource: number,
    well_stats: { adamantium: number, mana: number, elixir: number, upgraded: boolean },
    island_stats: { owner: number, flip_progress: number, locations: number[], is_accelerated: boolean, accelerated_tiles: Set<number> } | undefined): void {
    let content: string = ""
    content += `X: <b>${xrel}</b>`  // + `<b>${xrel}</b>`.padStart(3) + ` (${x})`.padStart(3)
    content += ` | Y: <b>${yrel}</b>` // + `<b>${yrel}</b>`.padStart(3) + ` (${y})`.padStart(3)

    content += ' | Well: ' + `<b>${cst.RESOURCENAMES[resource]}${resource > 0 && well_stats.upgraded ? "*" : ""}</b>`
    if (resource > 0 && (well_stats.adamantium > 0 || well_stats.elixir > 0 || well_stats.mana > 0)) {
      content += ' ('
      if (well_stats.adamantium > 0) content += `Ad: ${well_stats.adamantium}, `
      if (well_stats.mana > 0) content += `Mn: ${well_stats.mana}, `
      if (well_stats.elixir > 0) content += `Ex: ${well_stats.elixir}, `
      content = content.substring(0, content.length - 2)
      content += ")"
    }

    if (island_stats) {
      content += ' | Island:'
      if (island_stats.owner > 0) {
        content += ` <b>${cst.TEAM_NAMES[island_stats.owner - 1]}</b> (${island_stats.flip_progress})`
        if (island_stats.is_accelerated)
          content += ' acc'
      } else {
        content += " <b>Free</b>"
      }
    }

    this.tileInfo.innerHTML = content
  }

  /**
   * Display an info string in the controls bar
   * "Robot ID id
   * Location: (x, y)
   * onDirt, carryDirt
   * Bytecodes Used: bytecodes"
   */
  setInfoString(id, x: number, y: number, hp: number, max_hp: number, /*dp: number,*/ bodyType: schema.BodyType, bytecodes: number, indicatorString: string, adamantium: number, mana: number, elixir: number, anchor: string | null): void {
    // console.log(carryDirt);
    if (!indicatorString)
      indicatorString = '&nbsp;'
    // 
    let infoString = `<span class="info-name"><span class="info-string">${cst.bodyTypeToString(bodyType)}</span> | ID:</span> <span class="info-num">${id}</span> | `
    infoString += `<span class="info-name">Location:</span> <span class="info-num">(${x}, ${y})</span>`
    if (bodyType != cst.HEADQUARTERS)
      infoString += ` | <span class="info-name">HP:</span> <span class="info-num">${hp}</span> / <span class="info-num">${max_hp}</span>`
    infoString += `<br>`
    // infoString += `<span class="info-name">DP:</span> <span class="info-num">${dp}</span> | `;    
    infoString += `<span class="info-name">Bytecodes Used:</span> <span class="info-num">${bytecodes}</span>`
    if (bodyType == cst.CARRIER || bodyType == cst.HEADQUARTERS) {
      infoString += ` | Adamantium: <span class="info-num">${adamantium}</span>`
      infoString += ` | Mana: <span class="info-num">${mana}</span>`
      infoString += ` | Elixir: <span class="info-num">${elixir}</span>`
      if (anchor)
        infoString += ` | Anchors: <span class="info-num">${anchor}</span>`
    }
    // if (parent !== undefined) infoString += ` | <span class="info-name">Parent:</span> <span class="info-num">${parent}</span>`;
    infoString += `<br><span class="info-name">Indicator String:</span> <span class="info-string"><span class="info-string">${indicatorString}</span></span>`

    // (${bodyType})<br>
    // Location: (${x}, ${y})<br>
    //Influence: ${influence}, Conviction: ${conviction} <br>
    //Bytecodes Used: ${bytecodes}, Flag ${flag}`;

    this.infoString.innerHTML = infoString
  }

  removeInfoString() {
    this.infoString.innerHTML = ""
  }
}
