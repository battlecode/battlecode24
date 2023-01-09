import { Config } from '../config';
import * as cst from '../constants';
import { AllImages } from '../imageloader';
import { gameworld, schema } from 'battlecode-playback';
import Runner from '../runner';
import Chart = require('chart.js');
import { HEADQUARTERS } from '../constants';
import { threadId } from 'worker_threads';

const blue_background_chart_rgb: Chart.ChartColor = 'rgba(54, 162, 235, 0)'
const blue_border_chart_rgb: Chart.ChartColor = 'rgb(108, 140, 188)'
const red_background_chart_rgb: Chart.ChartColor = 'rgba(255, 99, 132, 0)' 
const red_border_chart_rgb: Chart.ChartColor = 'rgb(131,24,27)'

const hex: Object = {
  1: "var(--red)",
  2: "var(--blue)"
};

type HeadQuarterBar = {
  bar: HTMLDivElement,
  headQuarter: HTMLSpanElement,
  //bid: HTMLSpanElement
};

type BuffDisplay = {
  numBuffs: HTMLSpanElement,
  buff: HTMLSpanElement
}

type IncomeDisplay = {
  adamantiumIncome: HTMLSpanElement
  elixirIncome: HTMLSpanElement
  manaIncome: HTMLSpanElement
}

/**
* Loads game stats: team name, votes, robot count
* We make the distinction between:
*    1) Team names - a global string identifier i.e. "Teh Devs"
*    2) Team IDs - each Battlecode team has a unique numeric team ID i.e. 0
*    3) In-game ID - used to distinguish teams in the current match only;
*       team 1 is red, team 2 is blue
*/
export default class Stats {

  readonly div: HTMLDivElement;
  private readonly images: AllImages;

  private readonly tourIndexJump: HTMLInputElement;

  private teamNameNodes: HTMLSpanElement[] = [];

  // Key is the team ID
  private robotImages: Map<string, Array<HTMLImageElement>> = new Map(); // the robot image elements in the unit statistics display 
  private robotTds: Map<number, Map<string, Map<number, HTMLTableCellElement>>> = new Map();

  private headQuarterBars: HeadQuarterBar[];
  private maxVotes: number;

  private incomeDisplays: IncomeDisplay[];

  private relativeBars: HTMLDivElement[];
  
  private buffDisplays: BuffDisplay[];
  
  private extraInfo: HTMLDivElement;
  
  private robotConsole: HTMLDivElement;
  
  private runner: Runner; //needed for file uploading in tournament mode
  
  private conf: Config;

  private tourneyUpload: HTMLDivElement;

  private incomeChartAdamantium: Chart;
  private incomeChartMana: Chart;
  private incomeChartElixir: Chart;

  private distribChartAdamantium: Chart;
  private distribChartMana: Chart;
  private distribChartElixir: Chart;

  private ECs: HTMLDivElement;
  
  private islandDisplays: HTMLSpanElement[];
  // Note: robot types and number of teams are currently fixed regardless of
  // match info. Keep in mind if we ever change these, or implement this less
  // statically.

  readonly robots: schema.BodyType[] = cst.bodyTypeList;

  constructor(conf: Config, images: AllImages, runner: Runner) {
    this.conf = conf;
    this.images = images;

    for (const robot in this.images.robots_high_quality) {
      let robotImages: Array<HTMLImageElement> = this.images.robots_high_quality[robot];
      this.robotImages[robot] = robotImages.map((image) => image.cloneNode() as HTMLImageElement);
    }
    
    this.div = document.createElement("div");
    this.tourIndexJump = document.createElement("input");
    this.runner = runner;

    let teamNames: Array<string> = ["?????", "?????"];
    let teamIDs: Array<number> = [1, 2];
    this.initializeGame(teamNames, teamIDs);
  }

  /**
   * Colored banner labeled with the given teamName
   */
  private teamHeaderNode(teamName: string, inGameID: number) {
    let teamHeader: HTMLDivElement = document.createElement("div");
    teamHeader.className += ' teamHeader';

    let teamNameNode = document.createElement('span');
    teamNameNode.innerHTML = teamName;
    teamHeader.style.backgroundColor = hex[inGameID];
    teamHeader.appendChild(teamNameNode);
    this.teamNameNodes[inGameID] = teamNameNode;
    return teamHeader;
  }

  /**
   * Create the table that displays the robot images along with their counts.
   * Uses the teamID to decide which color image to display.
   */
  private robotTable(teamID: number, inGameID: number): HTMLTableElement {
    let table: HTMLTableElement = document.createElement("table");
    table.setAttribute("align", "center");

    // Create the table row with the robot images
    let robotImages: HTMLTableRowElement = document.createElement("tr");
    robotImages.appendChild(document.createElement("td")); // blank header

    // Create the table row with the robot counts
    let robotCounts = {};

    for (let value in this.robotTds[teamID]) {
      robotCounts[value] = document.createElement("tr");
      const title = document.createElement("td");
      if (value === "count") title.innerHTML = "<b>Count</b>";
      if (value === "hp") title.innerHTML = "<b>Σ(HP)</b>";
      robotCounts[value].appendChild(title);
    }

    for (let robot of this.robots) {
      let tdRobot: HTMLTableCellElement = document.createElement("td");
      tdRobot.className = "robotSpriteStats";
      tdRobot.style.height = "50px";
      tdRobot.style.width = "50px";

      const img: HTMLImageElement = this.robotImages[robot][inGameID];
      img.style.width = "100%";
      img.style.height = "100%";
      // TODO: images

      tdRobot.appendChild(img);
      robotImages.appendChild(tdRobot);

      for (let value in this.robotTds[teamID]) {
        let tdCount: HTMLTableCellElement = this.robotTds[teamID][value][robot];
        robotCounts[value].appendChild(tdCount);
        // TODO: figure out what's going on here
        // if (robot === schema.BodyType.ENLIGHTENMENT_CENTER && value === "count") {
        //   tdCount.style.fontWeight = "bold";
        //   tdCount.style.fontSize = "18px";          
        // }
      }
    }
    table.appendChild(robotImages);
    for (let value in this.robotTds[teamID]) {
      table.appendChild(robotCounts[value]);
    }

    return table;
  }

  private initRelativeBars(teamIDs: Array<number>) {
    let metalIDs = [0, 1, 2];
    let colors = ["#963749", "#25b5bc", "#f413b1"];
    const relativeBars: HTMLDivElement[] = [];
    teamIDs.forEach((teamID: number) => metalIDs.forEach((id: number) => {
      const bar = document.createElement("div");
      // bar.style.backgroundColor = colors[id];
      bar.style.border = "5px solid " + ((teamID === teamIDs[0]) ? "var(--red)" : "var(--blue)");
      bar.style.width = `90%`;
      bar.className = "influence-bar";
      bar.innerText = "0%";
      bar.id = teamID.toString();
      relativeBars[2*id + ((teamIDs[0] === teamID)?0:1)] = bar;
    }));
    return relativeBars;
  }

  private createRelativeBarsRow(statName: string, rowIndex: number){
    const tr = document.createElement("tr");
    tr.style.width = "100%";

    const label = document.createElement('td');
    label.className = "stats-header";
    label.innerText = statName;

    const cell1 = document.createElement("td");
    cell1.style.width = "50%";
    cell1.appendChild(this.relativeBars[rowIndex * 2]);
    
    const cell2 = document.createElement("td");
    cell2.style.width = "50%";
    cell2.appendChild(this.relativeBars[rowIndex * 2 + 1]);

    tr.appendChild(label);
    tr.appendChild(cell1);
    tr.appendChild(cell2);

    return tr;
  }
  private updateRelBars(teamAdamantium: Array<number>, teamMana: Array<number>, teamElixir: Array<number>){
    for(var a = 0; a < teamAdamantium.length; a++){
      this.relativeBars[a].innerHTML = teamAdamantium[a].toString();
      this.relativeBars[a].style.width = (Math.max(teamAdamantium[0], teamAdamantium[1]) === 0 ? 90:(90.0*teamAdamantium[a]/Math.max(teamAdamantium[0], teamAdamantium[1]))).toString() + "%";
    }
    for(var a = 0; a < teamMana.length; a++){
      this.relativeBars[a+2].innerHTML = teamMana[a].toString();
      this.relativeBars[a+2].style.width = (Math.max(teamMana[0], teamMana[1]) === 0 ? 90:(90.0*teamMana[a]/Math.max(teamMana[0], teamMana[1]))).toString() + "%";
    }
    for(var a = 0; a < teamElixir.length; a++){
      this.relativeBars[a+4].innerHTML = teamElixir[a].toString();
      this.relativeBars[a+4].style.width = (Math.max(teamElixir[0], teamElixir[1]) === 0 ? 90:(90.0*teamElixir[a]/Math.max(teamElixir[0], teamElixir[1]))).toString() + "%";
    }
  }

  private initIncomeDisplays(teamIDs: Array<number>) {
    const incomeDisplays: IncomeDisplay[] = [];
    teamIDs.forEach((id: number) => {
      const adamantiumIncome = document.createElement("div");
      const manaIncome = document.createElement("div");
      const elixirIncome = document.createElement("div");
      adamantiumIncome.style.color = hex[id];
      adamantiumIncome.style.fontWeight = "bold";
      adamantiumIncome.textContent = "Ad: 0";
      adamantiumIncome.style.padding = "10px";
      manaIncome.style.color = hex[id];
      manaIncome.style.fontWeight = "bold";
      manaIncome.textContent = "MN: 0";
      manaIncome.style.padding = "10px";
      elixirIncome.style.color = hex[id];
      elixirIncome.style.fontWeight = "bold";
      elixirIncome.textContent = "EL: 0";
      elixirIncome.style.padding = "10px";
      incomeDisplays[id] = {adamantiumIncome: adamantiumIncome,  manaIncome: manaIncome, elixirIncome: elixirIncome};
    });
    return incomeDisplays;
  }

  private getIncomeDisplaysElement(teamIDs: Array<number>): HTMLElement {
    const table = document.createElement("table");
    table.id = "income-table";
    table.style.width = "100%";

    const title = document.createElement('td');
    title.colSpan = 4;
    const label = document.createElement('div');
    label.className = "stats-header";
    label.innerText = 'Total Resource Income Per Turn';

    const row = document.createElement("tr");

    const cellAdamantium = document.createElement("td");
    teamIDs.forEach((id: number) => {
      
      // cell.appendChild(document.createTextNode("1.001"));
      // cell.appendChild(this.buffDisplays[id].numBuffs);
      // cell.appendChild(document.createTextNode(" = "));
      cellAdamantium.appendChild(this.incomeDisplays[id].adamantiumIncome);
      row.appendChild(cellAdamantium);
    });

    
    const cellMana = document.createElement("td");
    teamIDs.forEach((id: number) => {
      
      // cell.appendChild(document.createTextNode("1.001"));
      // cell.appendChild(this.buffDisplays[id].numBuffs);
      // cell.appendChild(document.createTextNode(" = "));
      cellMana.appendChild(this.incomeDisplays[id].manaIncome);
      row.appendChild(cellMana);
    });
    const cellElixir = document.createElement("td");
    teamIDs.forEach((id: number) => {
      
      // cell.appendChild(document.createTextNode("1.001"));
      // cell.appendChild(this.buffDisplays[id].numBuffs);
      // cell.appendChild(document.createTextNode(" = "));
      cellElixir.appendChild(this.incomeDisplays[id].elixirIncome);
      row.appendChild(cellElixir);
    });
    title.appendChild(label);
    table.appendChild(title);
    table.appendChild(row);

    return table;
  }

  private getIncomeManaGraph() {
    const canvas = document.createElement("canvas");
    canvas.id = "manaGraph";
    canvas.className = "graph";
    return canvas;
  }

  private getIncomeAdamantiumGraph() {
    const canvas = document.createElement("canvas");
    canvas.id = "adamantiumGraph";
    canvas.className = "graph";
    return canvas;
  }

  private getIncomeElixirGraph() {
    const canvas = document.createElement("canvas");
    canvas.id = "elixirGraph";
    canvas.className = "graph";
    return canvas;
  }

  private getDistribElixirGraph() {
    const canvas = document.createElement("canvas");
    canvas.id = "elixirDistirbGraph";
    canvas.className = "graph";
    return canvas;
  }

  private getDistribAdamantiumGraph() {
    const canvas = document.createElement("canvas");
    canvas.id = "adamantiumDistirbGraph";
    canvas.className = "graph";
    return canvas;
  }

  private getDistribManaGraph() {
    const canvas = document.createElement("canvas");
    canvas.id = "manaDistirbGraph";
    canvas.className = "graph";
    return canvas;
  }

  private getECDivElement() {
    const div = document.createElement('div');
    const label = document.createElement('div');
    label.className = "stats-header";
    label.innerText = 'HeadQuarter Status';
    div.appendChild(label);
    div.appendChild(this.ECs);
    return div;
  }


  private initIslandDisplays(teamIDs: Array<number>) {
    const islandDisplay: HTMLSpanElement[] = [];
    teamIDs.forEach((id: number) => {
      const adamantiumIncome = document.createElement("span");
      adamantiumIncome.style.color = hex[id];
      adamantiumIncome.style.fontWeight = "bold";
      adamantiumIncome.textContent = "0";
      adamantiumIncome.style.padding = "10px";
      islandDisplay[id] = adamantiumIncome
    });
    return islandDisplay;
  }

  private getIslandDisplaysElement(teamIDs: Array<number>): HTMLElement {
    const table = document.createElement("table");
    table.id = "islands-table";
    table.style.width = "100%";

    const title = document.createElement('td');
    title.colSpan = 4;
    const label = document.createElement('div');
    label.className = "stats-header";
    label.innerText = 'Islands owned by each team';

    const row = document.createElement("tr");

    const cellIsland = document.createElement("td");
    teamIDs.forEach((id: number) => {

      cellIsland.appendChild(this.islandDisplays[id]);
      row.appendChild(cellIsland);
    });

    title.appendChild(label);
    table.appendChild(title);
    table.appendChild(row);

    return table;
  }
  
  // private drawBuffsGraph(ctx: CanvasRenderingContext2D, upto: number) {
  //   ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
  //   // draw axes
  //   ctx.save();
  //   ctx.strokeStyle = "#000000";
  //   ctx.lineWidth = 0.02;
  //   ctx.moveTo(0, 1);
  //   ctx.lineTo(0, 0);
  //   ctx.stroke();
  //   ctx.moveTo(0, 1);
  //   ctx.lineTo(1, 1);
  //   ctx.stroke();

  //   const xscale = 1 / upto;
  //   const yscale = 1 / cst.buffFactor(upto);

  //   for (let i = 0; i <= upto; i++) {
  //     ctx.moveTo(i * xscale, 1 - cst.buffFactor(i) * yscale);
  //     ctx.lineTo(i * xscale, 1 - cst.buffFactor(i + 1) * yscale);
  //   }
  //   ctx.stroke();

  //   ctx.restore();
  // }

  // private plotBuff(ctx: CanvasRenderingContext2D, upto: number, buff1: number, buff2: number) {
  //   const xscale = 1 / upto;
  //   const yscale = 1 / cst.buffFactor(upto);

  //   ctx.save();

  //   ctx.fillStyle = hex[1];
  //   ctx.font = "0.1px Comic Sans MS";
  //   //  ctx.moveTo(buff1*xscale, cst.buffFactor(buff1)*yscale);
  //   ctx.fillText("R", buff1 * xscale, 1 - cst.buffFactor(buff1) * yscale + 0.08);
  //   // ctx.arc(buff1*xscale, cst.buffFactor(buff1)*yscale, 0.02, 0, 2*Math.PI);
  //   // ctx.fill();

  //   ctx.fillStyle = hex[2];
  //   ctx.fillText("B", buff2 * xscale, 1 - cst.buffFactor(buff2) * yscale - 0.04);

  //   ctx.moveTo(buff2 * xscale, cst.buffFactor(buff2) * yscale - 0.05);
  //   // ctx.arc(buff1*xscale, cst.buffFactor(buff2)*yscale - 0.05, 0.02, 0, 2*Math.PI);
  //   // ctx.fill();
  //   ctx.restore();
  // }

  /**
   * Clear the current stats bar and reinitialize it with the given teams.
   */
  initializeGame(teamNames: Array<string>, teamIDs: Array<number>) {
    // Remove the previous match info
    while (this.div.firstChild) {
      this.div.removeChild(this.div.firstChild);
    }
    this.relativeBars = [];
    this.maxVotes = 750;

    this.div.appendChild(document.createElement("br"));
    if (this.conf.tournamentMode) {
      // FOR TOURNAMENT
      this.tourneyUpload = document.createElement('div');
      
      let uploadButton = this.runner.getUploadButton();
      let tempdiv = document.createElement("div");
      tempdiv.className = "upload-button-div";
      tempdiv.appendChild(uploadButton);
      this.tourneyUpload.appendChild(tempdiv);

      // add text input field
      this.tourIndexJump.type = "text";
      this.tourIndexJump.onkeyup = (e) => { this.tourIndexJumpFun(e) };
      this.tourIndexJump.onchange = (e) => { this.tourIndexJumpFun(e) };
      this.tourneyUpload.appendChild(this.tourIndexJump);

      this.div.appendChild(this.tourneyUpload);
    }

    this.extraInfo = document.createElement('div');
    this.extraInfo.className = "extra-info";
    this.div.appendChild(this.extraInfo);

    // Populate with new info
    // Add a section to the stats bar for each team in the match
    for (var index = 0; index < teamIDs.length; index++) {
      // Collect identifying information
      let teamID = teamIDs[index];
      let teamName = teamNames[index];
      let inGameID = index + 1; // teams start at index 1
      this.robotTds[teamID] = new Map();

      // A div element containing all stats information about this team
      let teamDiv = document.createElement("div");

      // Create td elements for the robot counts and store them in robotTds
      // so we can update these robot counts later; maps robot type to count
      for (let value of ["count", "hp"]) {
        this.robotTds[teamID][value] = new Map<number, HTMLTableCellElement>();
        for (let robot of this.robots) {
          let td: HTMLTableCellElement = document.createElement("td");
          td.innerHTML = "0";
          this.robotTds[teamID][value][robot] = td;
        }
      }

      // Add the team name banner and the robot count table
      teamDiv.appendChild(this.teamHeaderNode(teamName, inGameID));
      teamDiv.appendChild(this.robotTable(teamID, inGameID));

      this.div.appendChild(teamDiv);
    }

    this.div.appendChild(document.createElement("hr"));

    // Add stats table
    /*let archonnums: Array<number> = [1, 2];
    teamIDs.forEach((id: number) => {
      archonnums[id] = (this.robotTds[id]["count"][ARCHON].inner == undefined) ? 0 : this.robotTds[id]["count"][ARCHON].inner;
      console.log(archonnums[id]);
    });*/
    // Create table structure
    const relativeBarsTable = document.createElement("table");
    relativeBarsTable.style.margin = "1rem 0 1rem 0";
    const relativeBarsHeader = document.createElement("thead");
    relativeBarsTable.appendChild(relativeBarsHeader);
    const relativeBarsHeaderRow = document.createElement("tr");
    relativeBarsHeader.appendChild(relativeBarsHeaderRow);

    // Create blue/red team header
    relativeBarsHeaderRow.appendChild(document.createElement("td")); // Empty cell
    const redTeamHeader = document.createElement("td");
    redTeamHeader.className = "stats-header";
    redTeamHeader.innerText = "Red";
    const blueTeamHeader = document.createElement("td");
    blueTeamHeader.className = "stats-header";
    blueTeamHeader.innerText = "Blue";
    relativeBarsHeaderRow.appendChild(redTeamHeader);
    relativeBarsHeaderRow.appendChild(blueTeamHeader);

    // Create resource rows
    const relativeBarsParent = document.createElement("tbody");
    relativeBarsParent.style.width = "100%";
    relativeBarsTable.appendChild(relativeBarsParent);
    this.div.appendChild(relativeBarsTable);

    this.relativeBars = this.initRelativeBars(teamIDs);
    const relativeBarsElements = [
      this.createRelativeBarsRow("Adamantium", 0),
      this.createRelativeBarsRow("Mana", 1),
      this.createRelativeBarsRow("Elixir", 2)
    ];
    relativeBarsElements.forEach((relBar: HTMLDivElement) => { relativeBarsParent.appendChild(relBar);});

    this.incomeDisplays = this.initIncomeDisplays(teamIDs);
    const incomeElement = this.getIncomeDisplaysElement(teamIDs);
    this.div.appendChild(incomeElement);

    const graphs = document.createElement("div");
    graphs.style.display = 'flex';
    graphs.style.flexDirection = "column";
    
    const row1=  document.createElement("div");
    row1.style.display = 'flex';
    row1.style.flexDirection = "row";
    // Adamantium
    const adamantiumWrapper = document.createElement("div");
    adamantiumWrapper.style.width = "50%";
    const canvasElementAdamantium = this.getIncomeAdamantiumGraph();
    adamantiumWrapper.appendChild(canvasElementAdamantium);    
    row1.appendChild(adamantiumWrapper);

    // Mana
    const manaWrapper = document.createElement("div");
    manaWrapper.style.width = "50%";
    const canvasElementMana = this.getIncomeManaGraph();
    manaWrapper.appendChild(canvasElementMana);    
    row1.appendChild(manaWrapper);

    graphs.append(row1);

    const row2=  document.createElement("div")
    row2.style.display = 'flex';
    row2.style.flexDirection = "row";
    row2.style.justifyContent = "center";

    // Elixir
    const elixirWrapper = document.createElement("div");
    elixirWrapper.style.width = "50%";
    const canvasElementElixir = this.getIncomeElixirGraph();
    elixirWrapper.appendChild(canvasElementElixir);    
    row2.append(elixirWrapper)
    graphs.append(row2);



    const row3=  document.createElement("div")
    row3.style.display = 'flex';
    row3.style.flexDirection = "row";
    row3.style.justifyContent = "center";


    


    const adamantiumDistribWrapper = document.createElement("div");
    adamantiumDistribWrapper.style.width = "50%";
    const canvasElementDstirbAdamantium = this.getDistribAdamantiumGraph();
    adamantiumDistribWrapper.appendChild(canvasElementDstirbAdamantium);    
    row3.append(adamantiumDistribWrapper)

    const manaDistribWrapper = document.createElement("div");
    manaDistribWrapper.style.width = "50%";
    const canvasElementDstirbMana = this.getDistribManaGraph();
    manaDistribWrapper.appendChild(canvasElementDstirbMana);    
    row3.append(manaDistribWrapper)

    graphs.append(row3);

    const row4=  document.createElement("div")
    row4.style.display = 'flex';
    row4.style.flexDirection = "row";
    row4.style.justifyContent = "center";


    const elixirDistribWrapper = document.createElement("div");
    elixirDistribWrapper.style.width = "50%";
    const canvasElementDstirbElixir = this.getDistribElixirGraph();
    elixirDistribWrapper.appendChild(canvasElementDstirbElixir);    
    row4.append(elixirDistribWrapper)

    
    graphs.append(row4);
    this.div.appendChild(graphs);
    
    this.incomeChartAdamantium = new Chart(canvasElementAdamantium, {
      type: 'line',
      data: {
          datasets: [{
            label: 'Red Adamantium',
            lineTension: 0,
            data: [],
            backgroundColor: red_background_chart_rgb,
            borderColor: red_border_chart_rgb,
            pointRadius: 0,
          },
          {
            label: 'Blue Adamantium',
            lineTension: 0,
            data: [],
            backgroundColor: blue_background_chart_rgb,
            borderColor: blue_border_chart_rgb,
            pointRadius: 0,
          }]
      },
      options: {
          aspectRatio: 0.75,
          animation: {
            duration: 0
          },
          scales: {
            xAxes: [{
              type: 'linear',
              ticks: {
                beginAtZero: true
            },
              scaleLabel: {
                display: true,
                labelString: "Turn"
              }
            }],
              yAxes: [{
                type: 'linear',
                  ticks: {
                      beginAtZero: true
                  }
              }]
          }
      }
    });

    this.incomeChartMana = new Chart(canvasElementMana, {
      type: 'line',
      data: {
          datasets: [
          {
            label: 'Red Mana',
            lineTension: 0,
            data: [],
            backgroundColor: red_background_chart_rgb,
            borderColor: red_border_chart_rgb,
            pointRadius: 0,
          },
          {
            label: 'Blue Mana',
            lineTension: 0,
            data: [],
            backgroundColor: blue_background_chart_rgb,
            borderColor: blue_border_chart_rgb,
            pointRadius: 0,
          }]
      },
      options: {
          aspectRatio: 0.75,
          animation: {
            duration: 0
          },
          scales: {
            xAxes: [{
              type: 'linear',
              ticks: {
                beginAtZero: true
            },
              scaleLabel: {
                display: true,
                labelString: "Turn"
              }
            }],
              yAxes: [{
                type: 'linear',
                  ticks: {
                      beginAtZero: true
                  }
              }]
          }
      }
    });

    this.incomeChartElixir = new Chart(canvasElementElixir, {
      type: 'line',
      data: {
          datasets: [{
            label: 'Red Elixir',
            lineTension: 0,
            data: [],
            backgroundColor: red_background_chart_rgb,
            borderColor: red_border_chart_rgb,
            pointRadius: 0,
          },
          {
            label: 'Blue Elixir',
            lineTension: 0,
            data: [],
            backgroundColor: blue_background_chart_rgb,
            borderColor: blue_border_chart_rgb,
            pointRadius: 0,
          }]
      },
      options: {
          aspectRatio: 0.75,
          animation: {
            duration: 0
          },
          scales: {
            xAxes: [{
              type: 'linear',
              ticks: {
                beginAtZero: true
            },
              scaleLabel: {
                display: true,
                labelString: "Turn"
              }
            }],
              yAxes: [{
                type: 'linear',
                  ticks: {
                      beginAtZero: true
                  }
              }]
          }
      }
    });

    this.distribChartElixir = new Chart(canvasElementDstirbElixir, {
      type: 'bar',
      data: {
        labels: ['HQs', 'HQs', 'robots', 'robots'],
        datasets: [
          {
            //label: "Population (millions)",
            backgroundColor: [blue_background_chart_rgb, red_background_chart_rgb, blue_background_chart_rgb, red_background_chart_rgb],
            borderColor: [blue_border_chart_rgb, red_border_chart_rgb, blue_border_chart_rgb, red_border_chart_rgb],
            data: [0, 0, 0, 0],
            borderWidth: 1,
          }
        ]
        
      } ,
    
    options: {
      aspectRatio: 0.75,
      scales: {
        yAxes: [{
          ticks: {
            beginAtZero: true
          }
        }]
       },
      legend: { display: false },
      title: {
        display: true,
        text: 'Elixir Distribution'
      }
    }
      
    })

    this.distribChartMana = new Chart(canvasElementDstirbMana, {
      type: 'bar',
      data: {
        labels: ['HQs', 'HQs', 'robots', 'robots'],
        datasets: [
          {
            //label: "Population (millions)",
            backgroundColor: [blue_background_chart_rgb, red_background_chart_rgb, blue_background_chart_rgb, red_background_chart_rgb],
            borderColor: [blue_border_chart_rgb, red_border_chart_rgb, blue_border_chart_rgb, red_border_chart_rgb],
            data: [0, 0, 0, 0],
            borderWidth: 1,
          }
        ]
        
      } ,
    options: {
      aspectRatio: 0.75,

      scales: {
        yAxes: [{
          ticks: {
            beginAtZero: true
          }
        }]
       },
      legend: { display: false },
      title: {
        display: true,
        text: 'Mana Distribution'
      }
    }
      
    })

    this. distribChartAdamantium= new Chart(canvasElementDstirbAdamantium, {
      type: 'bar',
      data: {
        labels: ['HQs', 'HQs', 'robots', 'robots'],
        datasets: [
          {
            //label: "Population (millions)",
            backgroundColor: [blue_background_chart_rgb, red_background_chart_rgb, blue_background_chart_rgb, red_background_chart_rgb],
            borderColor: [blue_border_chart_rgb, red_border_chart_rgb, blue_border_chart_rgb, red_border_chart_rgb],
            data: [0, 0, 0, 0],
            borderWidth: 1,
          }
        ],
      } ,
    
    options: {
      aspectRatio: 0.75,

      scales: {
        yAxes: [{
          ticks: {
            beginAtZero: true
          }
        }]
       },
      legend: { display: false },
      title: {
        display: true,
        text: 'Adamantium Distribution'
      }
    }
      
    })


    this.ECs = document.createElement("div");
    this.ECs.style.height = "100px";
    this.ECs.style.display = "flex";
    this.div.appendChild(this.getECDivElement());

    this.div.appendChild(document.createElement("br"));
    
    this.islandDisplays = this.initIslandDisplays(teamIDs);
    const islandElement = this.getIslandDisplaysElement(teamIDs);
    this.div.appendChild(islandElement);

  }

  tourIndexJumpFun(e) {
    if (e.keyCode === 13) {
      var h = +this.tourIndexJump.value.trim().toLowerCase();
      this.runner.seekTournament(h - 1);
    }
  }

  /**
   * Change the robot count on the stats bar
   */
  setRobotCount(teamID: number, robotType: schema.BodyType, count: number) {
    let td: HTMLTableCellElement = this.robotTds[teamID]["count"][robotType];
    td.innerHTML = String(count);
  }

  /**
   * Change the robot HP (previously conviction) on the stats bar
   */
  setRobotHP(teamID: number, robotType: schema.BodyType, HP: number, totalHP: number) {
    let td: HTMLTableCellElement = this.robotTds[teamID]["hp"][robotType];
    td.innerHTML = String(HP);

    let img = this.robotImages[robotType][teamID];

    const size = (55 + 45 * HP / totalHP);
    img.style.width = size + "%";
    img.style.height = size + "%";
  }

  /**
   * Change the robot influence on the stats bar
   */
  /**### setRobotInfluence(teamID: number, robotType: schema.BodyType, influence: number) {
    let td: HTMLTableCellElement = this.robotTds[teamID]["influence"][robotType];
    td.innerHTML = String(influence);
  }*/

  /**
   * Change the votes of the given team
   */
  setVotes(teamID: number, count: number) {
    // TODO: figure out if statbars.get(id) can actually be null??
    const statBar: HeadQuarterBar = this.headQuarterBars[teamID];
    statBar.headQuarter.innerText = String(count);
    this.maxVotes = Math.max(this.maxVotes, count);
    statBar.bar.style.width = `${Math.min(100 * count / this.maxVotes, 100)}%`;

    // TODO add reactions to relative bars
    // TODO get total votes to get ratio
    // this.relBars[teamID].width;

    // TODO winner gets star?
    // if (this.images.star.parentNode === statBar.bar) {
    //   this.images.star.remove();
    // }
  }

  /** setTeamInfluence(teamID: number, influence: number, totalInfluence: number) {
    const relBar: HTMLDivElement = this.relativeBars[teamID];
    relBar.innerText = String(influence);
    if (totalInfluence == 0) relBar.style.width = '50%';
    else relBar.style.width = String(Math.round(influence * 100 / totalInfluence)) + "%";
  }*/

  setIncome(teamID: number, teamStats: gameworld.TeamStats, turn: number, forceUpdate: boolean) { // incomes
    this.incomeDisplays[teamID].adamantiumIncome.textContent =
      "Ad: " + String((teamStats.adamantiumIncomeDataset[teamStats.adamantiumIncomeDataset.length - 1] ?? { y: 0 }).y.toFixed(2)); // change incomeDisplays later
    this.incomeDisplays[teamID].elixirIncome.textContent =
      "El: " + String((teamStats.elixirIncomeDataset[teamStats.elixirIncomeDataset.length - 1] ?? { y: 0 }).y.toFixed(2));
    this.incomeDisplays[teamID].manaIncome.textContent =
      "Mn: " + String((teamStats.manaIncomeDataset[teamStats.manaIncomeDataset.length - 1] ?? { y: 0 }).y.toFixed(2));
    
    // We check (turn - 1) here because the datasets get updated every 10 turns, so they will be visible to the graphs
    // starting on the 11th turn
    if (forceUpdate || (turn - 1) % 10 == 0) {
      //@ts-ignore
      this.incomeChartAdamantium.data.datasets![teamID - 1].data = teamStats.adamantiumIncomeDataset;
      //@ts-ignore
      this.incomeChartMana.data.datasets![teamID - 1].data = teamStats.manaIncomeDataset;
      //@ts-ignore
      this.incomeChartElixir.data.datasets![teamID - 1].data = teamStats.elixirIncomeDataset;

      this.incomeChartAdamantium.update();
      this.incomeChartMana.update();
      this.incomeChartElixir.update();
    }
  }
  updateDistributionBars(resources){
        
    var redHQsElixir = resources[1]["El"]["with_HQ"].reduce((partialSum, a) => partialSum + a, 0);
    var blueHQsElixir = resources[2]["El"]["with_HQ"].reduce((partialSum, a) => partialSum + a, 0);
    var redRobotsElixir = resources[1]["El"]["with_robots"];
    var blueRobotsElixir = resources[2]["El"]["with_robots"];
    
    
    this.distribChartElixir.data.datasets![0].data = [blueHQsElixir, redHQsElixir, blueRobotsElixir, redRobotsElixir];


    var redHQsMana = resources[1]["Mn"]["with_HQ"].reduce((partialSum, a) => partialSum + a, 0);
    var blueHQsMana = resources[2]["Mn"]["with_HQ"].reduce((partialSum, a) => partialSum + a, 0);
    var redRobotsMana = resources[1]["Mn"]["with_robots"];
    var blueRobotsMana = resources[2]["Mn"]["with_robots"];

    this.distribChartMana.data.datasets![0].data = [blueHQsMana, redHQsMana, blueRobotsMana, redRobotsMana];
   
    var redHQsAd = resources[1]["Ad"]["with_HQ"].reduce((partialSum, a) => partialSum + a, 0);
    var blueHQsAd = resources[2]["Ad"]["with_HQ"].reduce((partialSum, a) => partialSum + a, 0);
    var redRobotsAd = resources[1]["Ad"]["with_robots"];
    var blueRobotsAd = resources[2]["Ad"]["with_robots"];
   
    this.distribChartAdamantium.data.datasets![0].data = [blueHQsAd, redHQsAd, blueRobotsAd, redRobotsAd];

    this.distribChartAdamantium.update()
    this.distribChartElixir.update()
    this.distribChartMana.update()

  }
  
  updateBars(teamAdamantium: Array<number>, teamMana: Array<number>, teamElixir: Array<number>){
    this.updateRelBars(teamAdamantium, teamMana, teamElixir);
  }

  setWinner(teamID: number, teamNames: Array<string>, teamIDs: Array<number>) {
    const name = teamNames[teamIDs.indexOf(teamID)];
    this.teamNameNodes[teamID].innerHTML  = "<b>" + name + "</b> " +  `<span style="color: yellow">&#x1f31f</span>`;
  }

  /*setBid(teamID: number, bid: number) {
    // TODO: figure out if statbars.get(id) can actually be null??
    const statBar: VoteBar = this.voteBars[teamID];
    statBar.bid.innerText = String(bid);
    // TODO add reactions to relative bars
    // TODO get total votes to get ratio
    // this.relBars[teamID].width;

    // TODO winner gets star?
    // if (this.images.star.parentNode === statBar.bar) {
    //   this.images.star.remove();
    // }
  }*/

  setExtraInfo(info: string) {
    this.extraInfo.innerHTML = info;
  }

  hideTourneyUpload() {
    console.log(this.tourneyUpload);
    this.tourneyUpload.style.display = this.tourneyUpload.style.display === "none" ? "" : "none";
  }

  resetECs() {
    while (this.ECs.lastChild) this.ECs.removeChild(this.ECs.lastChild);
    // console.log(this.ECs);
    this.ECs.innerHTML = "";
  }

  addEC(teamID: number, health: number) {
    const div = document.createElement("div");
    let size = 1.0/(1 + Math.exp(-(health/100))) + 0.3;
    div.style.width = (28*size).toString() + "px";
    div.style.height = (28*size).toString() + "px";
    div.style.position = 'releative';
    div.style.top = '50%';
    div.style.transform  = `translateY(-${50*size - 35}%)`;
    const img = /* img */this.images.robots_high_quality[cst.HEADQUARTERS][teamID].cloneNode() as HTMLImageElement;
    img.style.width = `${56 * size}px`;
    img.style.height = `${56 * size}px`; // update dynamically later
    // img.style.marginTop = `${28*size}px`;

    div.appendChild(img);
    this.ECs.appendChild(div);
  }

  setIsland(teamID: number, mapStats: gameworld.MapStats) { // island
    let ans = 0;
    for (let entry of Array.from(mapStats.island_stats.entries())) {
      let key = entry[0];
      let value = entry[1];
      ans += (value.owner == teamID?1:0)
  }
    this.islandDisplays[teamID].textContent =
      String(ans); // change islandDisplays later
  }
}
