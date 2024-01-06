const { contextBridge, ipcRenderer } = require('electron')

const invoke = (command, ...args) => ipcRenderer.invoke('electronAPI', command, ...args)

const electronAPI = {
    openScaffoldDirectory: (...args) => invoke('openScaffoldDirectory', ...args),
    getRootPath: (...args) => invoke('getRootPath', ...args),
    getJavas: (...args) => invoke('getJavas', ...args),
    exportMap: (...args) => invoke('exportMap', ...args),
    getServerVersion: (...args) => invoke('getServerVersion', ...args),
    path: {
        join: (...args) => invoke('path.join', ...args),
        relative: (...args) => invoke('path.relative', ...args),
        dirname: (...args) => invoke('path.dirname', ...args),
        getSeperator: (...args) => invoke('path.sep', ...args)
    },
    fs: {
        exists: (...args) => invoke('fs.existsSync', ...args),
        mkdir: (...args) => invoke('fs.mkdirSync', ...args),
        getFiles: (...args) => invoke('fs.getFiles', ...args)
    },
    child_process: {
        spawn: (...args) => invoke('child_process.spawn', ...args),
        kill: (...args) => invoke('child_process.kill', ...args),
        onStdout: (callback) =>
            ipcRenderer.on('child_process.stdout', (event, data) => {
                callback(data)
            }),
        onStderr: (callback) =>
            ipcRenderer.on('child_process.stderr', (event, data) => {
                callback(data)
            }),
        onExit: (callback) =>
            ipcRenderer.on('child_process.exit', (event, data) => {
                callback(data)
            })
    }
}

contextBridge.exposeInMainWorld('electronAPI', electronAPI)
