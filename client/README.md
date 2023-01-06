# Battlecode Client 🌱

## Overview
Trivial wrapping folder for `playback` and `visualizer`. It handles universal configurations or scripts.

* `.editorconfig`
* `LICENSE`
* `.gitignore`
* `package.json`

### NPM config
Look at `package.json`.

This NPM module does not have any dependencies or meaningful output, but it is for wrapping scripts of `playback` and `visualizer` in one place.

 * `npm run install-all`: Installs npm packages in `playback` and `visualizer`. **Execute this when you start**
 * `npm run build`, `npm run build-playback`
 * `npm run electron`: Run the client in electron. You might want to run this most of the time.
 * `npm run watch`: Watch for the changes of `visualizer`. Note that it *does not watch* `playback`.
 * `npm run prod-electron`: Builds production versions of electron clients, for many OS's. Note that this will probably not work if run on your local machine, but it should work in our production build-and-release environment, which is on GitHub Actions / CI. 
 * `npm run prod-test`: Builds a production version of the electron client for your local machine's OS.
 * `npm run clean`: Cleans `dist/`. (output folder of `prod`)
