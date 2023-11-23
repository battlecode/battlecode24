import { useEffect, useRef, useState } from 'react'
import { BATTLECODE_YEAR, SERVER_MAPS } from '../../../constants'
import { NativeProcess, nativeAPI } from './native-api-wrapper'

type Scaffold = {
    maps: Set<string>
    players: Set<string>
    runMatch: (
        teamA: string,
        teamB: string,
        maps: Set<string>,
        onStart: (cmd: string) => void,
        onErr: (err: Error) => void,
        onExitNoError: () => void,
        onStdout: (data: string) => void,
        onStderr: (data: string) => void
    ) => void
    killMatch: () => void
}

const WINDOWS = process.env.ELECTRON && process.platform === 'win32'
const GRADLE_WRAPPER = WINDOWS ? 'gradlew.bat' : 'gradlew'

export function useScaffold(): [Scaffold | undefined, () => void, boolean, boolean] {
    const [scaffold, setScaffold] = useState<Scaffold | undefined>(undefined)
    const [loading, setLoading] = useState<boolean>(true)
    const [running, setRunning] = useState<boolean>(false)

    const procs = useRef<NativeProcess[]>([])

    function killProcs() {
        procs.current.forEach(function (proc) {
            proc.kill()
        })
        setRunning(false)
    }

    async function makeScaffold(scaffoldPath: string) {
        setLoading(false)

        if (!nativeAPI) throw new Error('Native API not available')

        const wrapperPath = nativeAPI.path.join(scaffoldPath, GRADLE_WRAPPER)
        if (!nativeAPI.fs.existsSync(wrapperPath)) {
            throw new Error(`Can't find gradle wrapper: ${wrapperPath}`)
        }

        const mapPath = nativeAPI.path.join(scaffoldPath, 'maps')
        if (!nativeAPI.fs.existsSync(mapPath)) {
            nativeAPI.fs.mkdirSync(mapPath)
        }

        let sourcePath = nativeAPI.path.join(scaffoldPath, 'src')
        if (!nativeAPI.fs.existsSync(sourcePath)) {
            sourcePath = nativeAPI.path.join(scaffoldPath, 'example-bots', 'src', 'main')

            if (!nativeAPI.fs.existsSync(sourcePath)) {
                throw new Error(`Can't find source path: ${sourcePath}`)
            }

            const players = getPlayers(sourcePath)
            const maps = getMaps(mapPath)

            Promise.all([players, maps]).then(([players, maps]) => {
                setScaffold({
                    maps,
                    players,
                    runMatch: (teamA, teamB, selectedMaps, onStart, onErr, onExitNoError, onStdout, onStderr) => {
                        {
                            if (!nativeAPI) throw new Error('Native API not available')

                            const options = [
                                `run`,
                                `-x`,
                                `unpackClient`,
                                `-PwaitForClient=true`,
                                `-PteamA=${teamA}`,
                                `-PteamB=${teamB}`,
                                `-Pmaps=${[...selectedMaps].join(',')}`,
                                `-PvalidateMaps=false`,
                                `-PenableProfiler=${false}`
                            ]

                            const proc = nativeAPI.child_process.spawn(wrapperPath, options, { cwd: scaffoldPath })

                            onStart(wrapperPath + ' ' + options.join('\n'))
                            proc.onStdout((data) => onStdout(data))
                            proc.onStderr((data) => onStderr(data))
                            proc.onClose((code) => {
                                setRunning(false)
                                if (code === 0) onExitNoError()
                                else onErr(new Error(`Non-zero exit code: ${code}`))
                            })
                            proc.onError((err) => {
                                onErr(err)
                            })
                            procs.current.push(proc)

                            setRunning(true)
                        }
                    },
                    killMatch: killProcs
                })
            })
        }
    }

    async function manuallySetupScaffold() {
        if (!nativeAPI) return
        setLoading(true)

        const path = await nativeAPI.openScaffoldDirectory()
        if (path) makeScaffold(path)
    }

    useEffect(() => {
        if (!nativeAPI) return

        nativeAPI.onBeforeAppQuit(function () {
            if (scaffold) scaffold.killMatch()
        })

        const scaffoldPath = findDefaultScaffoldPath()
        if (!scaffoldPath) return

        makeScaffold(scaffoldPath)
    }, [])

    return [scaffold, manuallySetupScaffold, loading, running]
}

function findDefaultScaffoldPath() {
    if (!nativeAPI) return null

    const appPath = nativeAPI.getRootPath()

    // npm run electron in client, if battlecode21-scaffold is located in same level as battlecode21
    const fromDev = nativeAPI.path.join(
        nativeAPI.path.dirname(nativeAPI.path.dirname(nativeAPI.path.dirname(appPath))),
        'battlecode' + (BATTLECODE_YEAR % 100) + '-scaffold'
    )
    // scaffold/client/Battlecode Client[.exe]
    const fromWin = nativeAPI.path.dirname(nativeAPI.path.dirname(appPath))
    // scaffold/client/resources/app.asar
    const from3 = nativeAPI.path.dirname(nativeAPI.path.dirname(nativeAPI.path.dirname(appPath)))
    // scaffold/Battlecode Client.app/Contents/Resources/app.asar
    const fromMac = nativeAPI.path.dirname(
        nativeAPI.path.dirname(nativeAPI.path.dirname(nativeAPI.path.dirname(nativeAPI.path.dirname(appPath))))
    )

    if (nativeAPI.fs.existsSync(nativeAPI.path.join(fromDev, GRADLE_WRAPPER))) {
        return fromDev
    } else if (nativeAPI.fs.existsSync(nativeAPI.path.join(from3, GRADLE_WRAPPER))) {
        return from3
    } else if (nativeAPI.fs.existsSync(nativeAPI.path.join(fromWin, GRADLE_WRAPPER))) {
        return fromWin
    } else if (nativeAPI.fs.existsSync(nativeAPI.path.join(fromMac, GRADLE_WRAPPER))) {
        return fromMac
    }
    return null
}

/**
 * Asynchronously get a list of available players in the scaffold.
 */
function getPlayers(sourcePath: string): Promise<Set<string>> {
    return new Promise((resolve, reject) => {
        walk(sourcePath, (files, err) => {
            if (err) reject(err)
            if (!files) resolve(new Set([]))
            return resolve(
                new Set(
                    files!
                        .filter(
                            (file) =>
                                file.endsWith(nativeAPI!.path.sep + 'RobotPlayer.java') ||
                                file.endsWith(nativeAPI!.path.sep + 'RobotPlayer.kt') ||
                                file.endsWith(nativeAPI!.path.sep + 'RobotPlayer.scala')
                        )
                        .map((file) => {
                            const relPath = nativeAPI!.path.relative(sourcePath, file)
                            return relPath
                                .replace(/.RobotPlayer\.[^/.]+$/, '')
                                .replace(new RegExp(WINDOWS ? '\\\\' : '/', 'g'), '.')
                                .replace(new RegExp('/', 'g'), '.')
                        })
                )
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
        nativeAPI!.fs.stat(mapPath, (stat: { isDirectory: () => any }, err?: Error) => {
            if (err != null || !stat || !stat.isDirectory()) {
                return resolve(new Set(SERVER_MAPS))
            }
            nativeAPI!.fs.readdir(mapPath, (files: string[] | undefined, err?: Error) => {
                if (err) reject(err)
                return resolve(
                    new Set(
                        files
                            ?.filter((file: string) => file.endsWith(mapExtension))
                            .map((file: string) => file.substring(0, file.length - mapExtension.length))
                            .concat(Array.from(SERVER_MAPS)) ?? []
                    )
                )
            })
        })
    })
}

function walk(dir: string, done: (paths?: string[], err?: Error) => void) {
    var results = new Array<string>()
    nativeAPI!.fs.readdir(dir, (list: string[], err?: Error) => {
        if (err) return done([], err)
        var errored = false
        var pending = list.length
        if (!pending) return done(results)
        list.forEach((file: string) => {
            file = nativeAPI!.path.resolve(dir, file)
            nativeAPI!.fs.stat(file, (stat: { isDirectory: () => any; isSymbolicLink: () => any }, err?: Error) => {
                if (errored) return
                if (err) {
                    errored = true
                    return done([], err)
                }

                if (stat && stat.isDirectory() && !stat.isSymbolicLink()) {
                    walk(file, (res, err) => {
                        if (errored) return
                        if (err) {
                            errored = true
                            return done([], err)
                        }

                        results = results.concat(res as string[])
                        if (!--pending) done(results)
                    })
                } else {
                    if (errored) return
                    results.push(file)
                    if (!--pending) done(results)
                }
            })
        })
    })
}
