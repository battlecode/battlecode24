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
        return result.canceled ? undefined : result.filePaths[0]
    },
    getRootPath: () => {
        return app.getAppPath()
    },
    onBeforeAppQuit: (callback) => {
        ipcRenderer.on('before-app-quit', callback)
    },
    path: {
        join: (...args) => {
            return path.join(...args)
        },
        relative: (from, to) => {
            return path.relative(from, to)
        },
        dirname: (dir) => {
            return path.dirname(dir)
        },
        resolve: (...args) => {
            return path.resolve(...args)
        },
        sep: path.sep
    },
    fs: {
        existsSync: (arg) => {
            return fs.existsSync(arg)
        },
        mkdirSync: (arg) => {
            return fs.mkdirSync(arg)
        },
        stat: (arg, callback) => {
            const callbackWrapper = (err, stats) =>
                callback(stats, err || undefined)
            return fs.stat(arg, callbackWrapper)
        },
        readdir: (arg, callback) => {
            const callbackWrapper = (err, files) =>
                callback(files, err || undefined)
            return fs.readdir(arg, callbackWrapper)
        }
    },
    child_process: {
        spawn: (command, args, options) => {
            const raw = child_process.spawn(command, args, options)
            const decoder = new TextDecoder()
            const wrapped = {
                onStdout: (callback) =>
                    raw.stdout.on('data', (data) => callback(decoder.decode(data))),
                onStderr: (callback) =>
                    raw.stderr.on('data', (data) => callback(decoder.decode(data))),
                onClose: (callback) => raw.on('close', callback),
                onError: (callback) => raw.on('error', callback),
                kill: () => raw.kill()
            }
            return wrapped
        }
    }
}

contextBridge.exposeInMainWorld('electronAPI', electronAPI)
