const { contextBridge, ipcRenderer } = require('electron');

const electronAPI = {
    openScaffoldDirectory: (...args) => ipcRenderer.invoke('electronAPI', 'openScaffoldDirectory', ...args),
    getRootPath: (...args) => ipcRenderer.invoke('electronAPI', 'getRootPath', ...args),
    path: {
        join: (...args) => ipcRenderer.invoke('electronAPI', 'path.join', ...args),
        relative: (...args) => ipcRenderer.invoke('electronAPI', 'path.relative', ...args),
        dirname: (...args) => ipcRenderer.invoke('electronAPI', 'path.dirname', ...args),
        resolve: (...args) => ipcRenderer.invoke('electronAPI', 'path.resolve', ...args),
        getSeperator: (...args) => ipcRenderer.invoke('electronAPI', 'path.sep', ...args),
    },
    fs: {
        exists: (...args) => ipcRenderer.invoke('electronAPI', 'fs.existsSync', ...args),
        mkdir: (...args) => ipcRenderer.invoke('electronAPI', 'fs.mkdirSync', ...args),
        getFiles: (...args) => ipcRenderer.invoke('electronAPI', 'fs.getFiles', ...args),
    },
    child_process: {
        spawn: (...args) => ipcRenderer.invoke('electronAPI', 'spawn', ...args),
        kill: (...args) => ipcRenderer.invoke('electronAPI', 'kill', ...args),
        onStdOut: (callback) => ipcRenderer.on('child_process.stdout', callback),
        onStdErr: (callback) => ipcRenderer.on('child_process.stderr', callback),
        onExit: (callback) => ipcRenderer.on('child_process.exit', callback),
    },
};

contextBridge.exposeInMainWorld('electronAPI', electronAPI);
