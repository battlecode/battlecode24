# Battlecode Visualizer 2023 📺

This is the game client for Battlecode 2023, which can run in your web browser or as a standalone application.

If you're a competitor and not interested in *developing* the client, you shouldn't do any of the things this document tells you to do. Proceed at your own risk.

### Tournament Mode

Change the value of `tournamentMode` in `visualizer/config.ts` from `false` to `true` to enable the tournament mode. The client will in tournament accept JSON files of a format specified in `visualizer/tournament.ts` and automatically step through the replays.

Step forward and backward in the tournament mode using hotkeys 'A' and 'D'.

### Developing

To get started:
```sh
$ npm install
```

To watch in a browser:
```sh
$ npm run watch
```

To watch using a standalone app (Electron):
```sh
$ npm run electron
```

When you `npm run electron`, you can set the default file to run when it is launched, by placing `default.bc23` in `/client` folder. So, it's loading `/client/default.bc23` file when it is launched.

To run the tests:
```sh
$ npm test
```
Note: Tests currently *don't run in a browser*; they run in node. They also don't understand webpack. That should change quickly.

All code and assets go in `src`, which is written in Typescript. Note that we're using webpack to bundle everything up; if you want the url of, say, an image, put the image at `src/images/image_file.png`, and then do `require('./images/image_file.png')`, which will return the URL of the image. If you want to reference another typescript file do a [standard typescript ES6 import](https://www.typescriptlang.org/docs/handbook/modules.html).

If you want to add a dependency, run `npm install --save package-name` and then `npm install --save @types/package-name` (for the typescript declarations). If `@types/package-name` doesn't exist, sacrifice a goat, or possibly a grad student.

Also note that this repo doesn't contain all of the client code. See `../playback`; that's the library that actually reads and replays the `.bc23` match files. This repo has everything else; video, sound, controls, etc.

If you've made a change in `../playback` and want to integrate it here, you need to do `npm install` again. It's a bit of a pain; and should probably be integrated in this folder at some point.

To delete the output of `prod` commands, run 

```sh
$ npm run clean
```

### Deploying

#### Non-Electron

To get a web-based version of the client, run:
```sh
$ npm run prod
```
This will bundle up all of the assets you want in the `out` folder. You can then embed the client in any web page you want:

- Mount the assets in `out` at `/out/` on your webserver.
- `<script src="/out/app.js"></script>`
- `<script>window.battlecode.mount(document.getElementById('battlecode-div'))</script>`

You now have a copy of the battlecode client running on your web page. See `src/app.ts` and `src/config.ts` to learn about the client's API.

#### Electron

Electron releases are produced in GitHub Actions, which runs `npm run prod-electron` (see `.github/workflows/release.yml`). Note that this command will probably fail if run from your local machine. 

If you want to test some sort of production electron release, do:

```sh
$ npm run prod-test
```

This will build the electron release for your own OS and architecture. Then, find it in the `dist` folder of `client`.
