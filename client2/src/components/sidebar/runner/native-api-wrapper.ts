export interface NativeProcess {
    onStdout: (callback: (data: string) => void) => void
    onStderr: (callback: (data: string) => void) => void
    onClose: (callback: (code: number) => void) => void
    onError: (callback: (err: Error) => void) => void
    kill: () => void
}

export type NativeAPI = {
    openScaffoldDirectory: () => Promise<string | undefined>
    getRootPath: () => string
    onBeforeAppQuit: (callback: () => void) => void
    path: {
        join: (...args: string[]) => string
        relative: (from: string, to: string) => string
        dirname: (dir: string) => string
        resolve: (...args: string[]) => string
        sep: string
    }
    fs: {
        existsSync: (arg: string) => boolean
        mkdirSync: (arg: string) => void
        stat: (
            arg: string,
            callback: (stats: { isDirectory: () => any; isSymbolicLink: () => any }, err?: Error) => void
        ) => void
        readdir: (arg: string, callback: (files: string[], err?: Error) => void) => void
    }
    child_process: {
        spawn: (command: string, args: string[] | undefined, options: any) => NativeProcess
    }
}

let nativeAPI: NativeAPI | undefined = undefined

// attempt to connect to electron
// @ts-ignore
if (window.electronAPI) {
    // @ts-ignore
    nativeAPI = window.electronAPI as NativeAPI
}

// verify that native api is setup if available
if (nativeAPI) {
    const requiredFunctions = ['openScaffoldDirectory', 'getRootPath', 'onBeforeAppQuit', 'path', 'fs', 'child_process']
    for (const func of requiredFunctions) {
        // @ts-ignore
        if (!nativeAPI[func]) {
            throw new Error(`Native API missing function: ${func}`)
        }
    }
}

export { nativeAPI }
