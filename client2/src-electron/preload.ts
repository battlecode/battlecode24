import { contextBridge, ipcRenderer, app, dialog } from 'electron'
import * as path from 'path'
import * as fs from 'fs'
import * as child_process from 'child_process'

export const electronAPI = {
    openScaffoldDirectory: async () => {
        const result = await dialog.showOpenDialog({
            title: 'Please select your battlecode-scaffold directory.',
            properties: ['openDirectory']
        })
        return result
    },
    getRootPath: () => {
        return app.getAppPath()
    },
    onBeforeAppQuit: (callback: (event: Electron.IpcRendererEvent, ...args: any[]) => void) => {
        ipcRenderer.on('before-app-quit', callback)
    },
    // selectively expose methods of the 'path' module
    path: {
        join: (...args: string[]) => {
            return path.join(...args)
        },
        relative: (from: string, to: string) => {
            return path.relative(from, to)
        },
        dirname: (dir: string) => {
            return path.dirname(dir)
        },
        resolve: (...args: string[]) => {
            return path.resolve(...args)
        },
        sep: path.sep
    },
    // selectively expose methods of the 'fs' module
    fs: {
        existsSync: (arg: fs.PathLike) => {
            return fs.existsSync(arg)
        },
        mkdirSync: (arg: fs.PathLike) => {
            return fs.mkdirSync(arg)
        },
        stat: (arg: fs.PathLike, callback: (err: NodeJS.ErrnoException | null, stats: fs.Stats) => void) => {
            return fs.stat(arg, callback)
        },
        readdir: (arg: fs.PathLike, callback: (err: NodeJS.ErrnoException | null, files: string[]) => void) => {
            return fs.readdir(arg, callback)
        },
        Stats: fs.Stats
    },
    // selectively expose methods of the 'child_process' module
    child_process: {
        spawn: (
            command: string,
            args: string[] | undefined,
            options: child_process.SpawnOptionsWithoutStdio | undefined
        ) => {
            return child_process.spawn(command, args, options)
        }
    }
}

contextBridge.exposeInMainWorld('electronAPI', electronAPI)
