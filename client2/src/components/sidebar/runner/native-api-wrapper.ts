export interface NativeProcess {
    onStdout: (callback: (data: string) => void) => void
    onStderr: (callback: (data: string) => void) => void
    onClose: (callback: (code: number) => void) => void
    onError: (callback: (err: Error) => void) => void
    kill: () => void
}

export type NativeAPI = {
    openScaffoldDirectory: () => Promise<string | undefined>
    getRootPath: () => Promise<string>
    path: {
        join: (...args: string[]) => Promise<string>
        relative: (from: string, to: string) => Promise<string>
        dirname: (dir: string) => Promise<string>
        resolve: (...args: string[]) => Promise<string>
        getSeperator: () => Promise<string>
    }
    fs: {
        exists: (arg: string) => Promise<boolean>
        mkdir: (arg: string) => Promise<void>
        getFiles: (path: string, recursive?: boolean) => Promise<string[]>
    }
    child_process: {
        spawn: (command: string, args: string[] | undefined, options: any) => Promise<number>
        kill: (pid: number) => Promise<void>
        onStdout: (callback: (pid: number, data: string) => void) => void
        onStderr: (callback: (pid: number, data: string) => void) => void
        onExit: (callback: (pid: number, code: number) => void) => void
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
    Object.keys(nativeAPI).forEach(function (key) {
        // @ts-ignore
        if (!nativeAPI[key]) {
            throw new Error(`Native API missing property: ${key}`)
        }
    })

    console.log('Native API available and verified')
}

export { nativeAPI }
