const { app, BrowserWindow, screen: electronScreen, ipcMain, dialog } = require('electron')
const isDev = require('electron-is-dev')
const path = require('path')
const fs = require('fs')
const javaFind = require('java-find')
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

app.on('ready', () => {
    if (!BrowserWindow.getAllWindows().length) {
        createMainWindow()
    }

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

const reformatJavaPath = (javaPath) => {
    if (!javaPath) return ''

    // Ensure that the found path is a jdk install
    if (!javaPath.toLowerCase().includes('jdk')) return ''

    const hasLeadingSlash = javaPath[0] == '/'

    // Ensure that the java path ends with 'Home' since that is what JAVA_HOME expects
    const items = javaPath.split(path.sep)
    while (items.length > 0) {
        if (items[items.length - 1] == 'Home') break
        items.pop()
    }

    return (hasLeadingSlash ? '/' : '') + path.join(...items)
}

const processes = new Map()
function killAllProcesses() {
    while (processes.size > 0) {
        const pid = processes.keys().next().value
        processes.get(pid).kill()
    }
}

const WINDOWS = process.platform === 'win32'
const GRADLE_WRAPPER = WINDOWS ? 'gradlew.bat' : 'gradlew'

ipcMain.handle('electronAPI', async (event, operation, ...args) => {
    switch (operation) {
        case 'openScaffoldDirectory': {
            const result = await dialog.showOpenDialog({
                title: 'Please select your battlecode-scaffold directory.',
                properties: ['openDirectory']
            })
            return result.canceled ? undefined : result.filePaths[0]
        }
        case 'getRootPath':
            return app.getAppPath()
        case 'getJavas': {
            const output = []
            const foundPaths = {}
            try {
                const javas = (await javaFind.getJavas()).filter((j) => j.version.major == 1 && j.version.minor == 8)
                for (const j of javas) {
                    const v = j.version
                    const displayStr = `${v.major}.${v.minor}.${v.patch}_${v.update} (${j.arch})`
                    const formattedPath = reformatJavaPath(j.path)
                    if (!formattedPath || formattedPath in foundPaths) continue
                    foundPaths[formattedPath] = true
                    output.push(displayStr)
                    output.push(formattedPath)
                }
            } catch {}
            return output
        }
        case 'exportMap': {
            const result = await dialog.showSaveDialog({
                title: 'Export map',
                defaultPath: args[1]
            })
            if (!result.canceled) {
                const path = result.filePath
                fs.writeFileSync(path, new Uint8Array(args[0]))
            }
            return
        }
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
            const javaPath = args[1]
            const flags = args[2]
            const wrapperPath = path.join(scaffoldPath, GRADLE_WRAPPER)
            const options = { cwd: scaffoldPath }
            if (javaPath) options.env = { JAVA_HOME: javaPath }
            const child = child_process.spawn(wrapperPath, flags, options)
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
            if (processes.has(pid)) {
                processes.get(pid).kill()
            }
            return
        }
        default:
            throw new Error('Invalid ipc API operation: ' + operation)
    }
})
