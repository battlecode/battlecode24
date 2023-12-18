const { app, BrowserWindow, screen: electronScreen, ipcMain, dialog } = require('electron')
const isDev = require('electron-is-dev')
const path = require('path')
const fs = require('fs')
const child_process = require('child_process')

let mainWindow

const createMainWindow = () => {
    mainWindow = new BrowserWindow({
        width: electronScreen.getPrimaryDisplay().workArea.width,
        height: electronScreen.getPrimaryDisplay().workArea.height,
        backgroundColor: 'white',
        webPreferences: {
            devTools: isDev,
            preload: path.join(__dirname, 'electron-bridge.js')
        }
    })
    const startURL = isDev ? 'http://localhost:3000' : `file://${path.join(__dirname, '../dist/index.html')}`

    mainWindow.loadURL(startURL)
    mainWindow.once('ready-to-show', () => mainWindow.show())
    mainWindow.on('closed', () => {
        mainWindow = null
    })
    mainWindow.on('new-window-for-tab', (event, url) => {
        event.preventDefault()
        mainWindow.loadURL(url)
    })
}

app.whenReady().then(() => {
    app.on('activate', () => {
        if (!BrowserWindow.getAllWindows().length) {
            createMainWindow()
        }
    })

    app.on('before-quit', (event) => {
        killAllProcesses()
    })

    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            app.quit()
        }
    })
})

const getFiles = (dir, recursive) => {
    const files = []
    for (const file of fs.readdirSync(dir)) {
        const fullPath = path.join(dir, file)
        const stat = fs.statSync(fullPath)
        if (stat.isDirectory() && !stat.isSymbolicLink()) {
            if (recursive) files.push(...getFiles(fullPath, recursive))
        } else {
            files.push(fullPath)
        }
    }
    return files
}

const processes = new Map()
function killAllProcesses() {
    while (processes.size > 0) {
        const pid = processes.keys().next().value
        processes.get(pid).kill()
    }
}

const WINDOWS = process.env.ELECTRON && process.platform === 'win32'
const GRADLE_WRAPPER = WINDOWS ? 'gradlew.bat' : 'gradlew'

ipcMain.handle('electronAPI', async (event, operation, ...args) => {
    switch (operation) {
        case 'openScaffoldDirectory':
            const result = await dialog.showOpenDialog({
                title: 'Please select your battlecode-scaffold directory.',
                properties: ['openDirectory']
            })
            return result.canceled ? undefined : result.filePaths[0]
        case 'getRootPath':
            return app.getAppPath()
        case 'path.join':
            return path.join(...args)
        case 'path.relative':
            return path.relative(...args)
        case 'path.dirname':
            return path.dirname(args[0])
        case 'path.sep':
            return path.sep
        case 'fs.existsSync':
            return fs.existsSync(args[0])
        case 'fs.mkdirSync':
            return fs.mkdirSync(args[0])
        case 'fs.getFiles':
            return getFiles(args[0], args[1] === 'true')
        case 'child_process.spawn': {
            const scaffoldPath = args[0]
            const flags = args[1]
            const wrapperPath = path.join(scaffoldPath, GRADLE_WRAPPER)
            const child = child_process.spawn(wrapperPath, flags, { cwd: scaffoldPath })
            const pid = child.pid.toString()

            processes.set(pid, child)

            child.stdout.on('data', (data) => {
                event.sender.send('child_process.stdout', { pid, data: data.toString() })
            })
            child.stderr.on('data', (data) => {
                event.sender.send('child_process.stderr', { pid, data: data.toString() })
            })
            child.on('exit', (code, signal) => {
                processes.delete(child.pid)
                event.sender.send('child_process.exit', {
                    pid,
                    code: (code ?? 0).toString(),
                    signal: (signal ?? 0).toString()
                })
            })

            return child.pid.toString()
        }
        case 'child_process.kill': {
            const pid = args[0]
            if (processes.has(pid)) processes.get(pid).kill()
            return
        }
        default:
            throw new Error('Invalid ipc API operation: ' + operation)
    }
})
