import { useEffect, useRef, useState } from 'react'
import { electron, fs, path, child_process } from './electron-modules'
import { BATTLECODE_YEAR, SERVER_MAPS } from '../../../constants'

export enum ScaffoldState {
    Connected = 'connected',
    NonElectron = 'non-electron',
    Disconnected = 'disconnected'
}
type Scaffold = {
    maps: Set<string>
    players: string[]
    runMatch: (
        teamA: string,
        teamB: string,
        maps: string[],
        onStart: (cmd: string) => void,
        onErr: (err: Error) => void,
        onExitNoError: () => void,
        onStdout: (data: string) => void,
        onStderr: (data: string) => void
    ) => void
    running: boolean
    killMatch: () => void
}

const WINDOWS = process.env.ELECTRON && process.platform === 'win32'
const GRADLE_WRAPPER = WINDOWS ? 'gradlew.bat' : 'gradlew'

export function useScaffold(): [ScaffoldState, Scaffold | undefined, () => void, boolean] {
    const [scaffold, setScaffold] = useState<Scaffold | undefined>(undefined)
    const [loading, setLoading] = useState<boolean>(true)
    const [state, setState] = useState<ScaffoldState>(ScaffoldState.NonElectron)

    const procs = useRef<any[]>([])

    function killProcs() {
        procs.current.forEach(function (proc) {
            proc.kill()
        })
        setScaffold({
            ...scaffold!,
            running: false
        })
    }

    async function makeScaffold(scaffoldPath: string) {
        setLoading(false)

        const wrapperPath = path.join(scaffoldPath, GRADLE_WRAPPER)
        if (!fs.existsSync(wrapperPath)) {
            setState(ScaffoldState.Disconnected)
            throw new Error(`Can't find gradle wrapper: ${wrapperPath}`)
        }

        const mapPath = path.join(scaffoldPath, 'maps')
        if (!fs.existsSync(mapPath)) {
            fs.mkdirSync(mapPath)
        }

        let sourcePath = path.join(scaffoldPath, 'src')
        if (!fs.existsSync(sourcePath)) {
            sourcePath = path.join(scaffoldPath, 'example-bots', 'src', 'main')

            if (!fs.existsSync(sourcePath)) {
                setState(ScaffoldState.Disconnected)
                throw new Error(`Can't find source path: ${sourcePath}`)
            }

            const players = getPlayers(sourcePath)
            const maps = getMaps(mapPath)

            Promise.all([players, maps]).then(([players, maps]) => {
                setScaffold({
                    maps,
                    players,
                    runMatch: (teamA, teamB, maps, onStart, onErr, onExitNoError, onStdout, onStderr) => {
                        {
                            const options = [
                                `run`,
                                `-x`,
                                `unpackClient`,
                                `-PwaitForClient=true`,
                                `-PteamA=${teamA}`,
                                `-PteamB=${teamB}`,
                                `-Pmaps=${maps.join(',')}`,
                                `-PvalidateMaps=false`,
                                `-PenableProfiler=${false}`
                            ]
                            const proc = child_process.spawn(wrapperPath, options, { cwd: scaffoldPath })
                            onStart(wrapperPath + ' ' + options.join('\n'))
                            const decoder = new window['TextDecoder']()
                            // @ts-ignore
                            proc.stdout.on('data', (data) => onStdout(decoder.decode(data)))
                            // @ts-ignore
                            proc.stderr.on('data', (data) => onStderr(decoder.decode(data)))
                            proc.on('close', (code) => {
                                if (code === 0) {
                                    onExitNoError()
                                } else {
                                    onErr(new Error(`Non-zero exit code: ${code}`))
                                }
                            })
                            proc.on('error', (err) => {
                                onErr(err)
                            })
                            // proc.on('pid-message', function (event, arg) {
                            //     console.log('Main:', arg)
                            //     this.pids.push(arg)
                            // })
                            procs.current.push(proc)

                            setScaffold({
                                ...scaffold!,
                                running: true
                            })
                        }
                    },
                    running: false,
                    killMatch: killProcs
                })
                setState(ScaffoldState.Connected)
            })
        }
    }

    function manuallySetupScaffold() {
        if (!process.env.ELECTRON) return
        setState(ScaffoldState.Disconnected)
        setLoading(true)

        electron.remote.dialog
            .showOpenDialog({
                title: 'Please select your battlecode-scaffold directory.',
                properties: ['openDirectory']
            })
            .then((result: any) => {
                if (result.filePaths.length > 0) makeScaffold(result.filePaths[0])
            })
    }

    useEffect(() => {
        if (!process.env.ELECTRON) return

        electron.remote.app.on('before-quit', function () {
            if (scaffold) scaffold.killMatch()
        })

        setState(ScaffoldState.Disconnected)
        const scaffoldPath = findDefaultScaffoldPath()
        if (!scaffoldPath) return

        makeScaffold(scaffoldPath)
    }, [])

    return [state, scaffold, manuallySetupScaffold, loading]
}

function findDefaultScaffoldPath() {
    if (!process.env.ELECTRON) return null

    const appPath = electron.remote.app.getAppPath()

    console.log('app path: ' + appPath)

    // npm run electron in client, if battlecode21-scaffold is located in same level as battlecode21
    const fromDev = path.join(
        path.dirname(path.dirname(path.dirname(appPath))),
        'battlecode' + (BATTLECODE_YEAR % 100) + '-scaffold'
    )
    // scaffold/client/Battlecode Client[.exe]
    const fromWin = path.dirname(path.dirname(appPath))
    // scaffold/client/resources/app.asar
    const from3 = path.dirname(path.dirname(path.dirname(appPath)))
    // scaffold/Battlecode Client.app/Contents/Resources/app.asar
    const fromMac = path.dirname(path.dirname(path.dirname(path.dirname(path.dirname(appPath)))))

    if (fs.existsSync(path.join(fromDev, GRADLE_WRAPPER))) {
        return fromDev
    } else if (fs.existsSync(path.join(from3, GRADLE_WRAPPER))) {
        return from3
    } else if (fs.existsSync(path.join(fromWin, GRADLE_WRAPPER))) {
        return fromWin
    } else if (fs.existsSync(path.join(fromMac, GRADLE_WRAPPER))) {
        return fromMac
    }
    return null
}

/**
 * Asynchronously get a list of available players in the scaffold.
 */
function getPlayers(sourcePath: string): Promise<string[]> {
    return new Promise((resolve, reject) => {
        walk(sourcePath, (err, files) => {
            if (err) reject(err)
            if (!files) resolve([])

            return resolve(
                files!
                    .filter(
                        (file) =>
                            file.endsWith(path.sep + 'RobotPlayer.java') ||
                            file.endsWith(path.sep + 'RobotPlayer.kt') ||
                            file.endsWith(path.sep + 'RobotPlayer.scala')
                    )
                    .map((file) => {
                        const relPath = path.relative(sourcePath, file)
                        return relPath
                            .replace(/.RobotPlayer\.[^/.]+$/, '')
                            .replace(new RegExp(WINDOWS ? '\\\\' : '/', 'g'), '.')
                            .replace(new RegExp('/', 'g'), '.')
                    })
            )
        })
    })
}

/**
 * Asynchronously get a list of map paths.
 */
function getMaps(mapPath: string): Promise<Set<string>> {
    const mapExtension = '.map' + (BATTLECODE_YEAR % 100)
    return new Promise((resolve, reject) => {
        fs.stat(mapPath, (err, stat) => {
            if (err != null || !stat || !stat.isDirectory()) {
                return resolve(new Set(SERVER_MAPS))
            }

            fs.readdir(mapPath, (err, files) => {
                if (err) reject(err)

                return resolve(
                    new Set(
                        files
                            .filter((file) => file.endsWith(mapExtension))
                            .map((file) => file.substring(0, file.length - mapExtension.length))
                            .concat(Array.from(SERVER_MAPS))
                    )
                )
            })
        })
    })
}

function walk(dir: string, done: (err: Error | null, paths?: string[]) => void) {
    var results = new Array<string>()
    fs.readdir(dir, (err, list) => {
        if (err) return done(err)
        var errored = false
        var pending = list.length
        if (!pending) return done(null, results)
        list.forEach((file) => {
            file = path.resolve(dir, file)
            fs.stat(file, (err, stat) => {
                if (errored) return
                if (err) {
                    errored = true
                    return done(err)
                }

                if (stat && stat.isDirectory() && !stat.isSymbolicLink()) {
                    walk(file, (err, res) => {
                        if (errored) return
                        if (err) {
                            errored = true
                            return done(err)
                        }

                        results = results.concat(res as string[])
                        if (!--pending) done(null, results)
                    })
                } else {
                    if (errored) return
                    results.push(file)
                    if (!--pending) done(null, results)
                }
            })
        })
    })
}
