const { contextBridge, ipcRenderer } = require('electron');

const electronAPI = {
    openScaffoldDirectory: (...args) => ipcRenderer.invoke('electronAPI', 'openScaffoldDirectory', ...args),
    getRootPath: (...args) => ipcRenderer.invoke('electronAPI', 'getRootPath', ...args),
    path: {
        join: (...args) => ipcRenderer.invoke('electronAPI', 'path.join', ...args),
        relative: (...args) => ipcRenderer.invoke('electronAPI', 'path.relative', ...args),
        dirname: (...args) => ipcRenderer.invoke('electronAPI', 'path.dirname', ...args),
        getSeperator: (...args) => ipcRenderer.invoke('electronAPI', 'path.sep', ...args),
    },
    fs: {
        exists: (...args) => ipcRenderer.invoke('electronAPI', 'fs.existsSync', ...args),
        mkdir: (...args) => ipcRenderer.invoke('electronAPI', 'fs.mkdirSync', ...args),
        getFiles: (...args) => ipcRenderer.invoke('electronAPI', 'fs.getFiles', ...args),
    },
    child_process: {
        spawn: (...args) => ipcRenderer.invoke('electronAPI', 'child_process.spawn', ...args),
        kill: (...args) => ipcRenderer.invoke('electronAPI', 'child_process.kill', ...args),
        onStdout: (callback) => ipcRenderer.on('child_process.stdout', (event, data) => { callback(data); }),
        onStderr: (callback) => ipcRenderer.on('child_process.stderr', (event, data) => { callback(data); }),
        onExit: (callback) => ipcRenderer.on('child_process.exit', (event, data) => { callback(data); }),
    },
};

contextBridge.exposeInMainWorld('electronAPI', electronAPI);
