# Battlecode Client 🌱
## Overview
- `src-tauri` - Contains Rust code to wrap Electron.js as an app via Tauri.
- `src`
  - `components` - React Components
  - `pages` - `Queue`, `Runner`, `Game` pages, and more
  - `playback` - Load up battlecode replay files (flatbuffers) into abstract game objects
  - `util` - Various Utility Tools

## Running Locally
To run the client locally:
1. `npm i` to install dependencies and prettier
3. `npm run fix-schema` to fix `TS2307: Cannot find module 'flatbuffers' or its corresponding type declarations.`
4. `npm run watch` to run the app.

Run `npx prettier [file].tsx --write` on any changed files to standardize the format. 
