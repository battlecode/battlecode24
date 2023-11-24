const { tauri } = window;

const tauriAPI = {
    openScaffoldDirectory: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'openScaffoldDirectory', args }),
    getRootPath: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'getRootPath', args }),
    path: {
        join: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'path.join', args }),
        relative: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'path.relative', args }),
        dirname: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'path.dirname', args }),
        getSeparator: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'path.sep', args }),
    },
    fs: {
        exists: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'fs.existsSync', args }),
        mkdir: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'fs.mkdirSync', args }),
        getFiles: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'fs.getFiles', args }),
    },
    child_process: {
        spawn: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'child_process.spawn', args }),
        kill: (...args) => tauri.promisified({ cmd: 'tauriAPI', operation: 'child_process.kill', args }),
        onStdout: (callback) => tauri.listen('child_process.stdout', (event, data) => callback(data)),
        onStderr: (callback) => tauri.listen('child_process.stderr', (event, data) => callback(data)),
        onExit: (callback) => tauri.listen('child_process.exit', (event, data) => callback(data)),
    },
};

window.tauriAPI = tauriAPI;
