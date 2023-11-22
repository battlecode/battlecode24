const { app, BrowserWindow, screen: electronScreen } = require('electron')
const isDev = require('electron-is-dev')
const path = require('path')

let mainWindow: Electron.CrossProcessExports.BrowserWindow | null

const createMainWindow = () => {
    mainWindow = new BrowserWindow({
        width: electronScreen.getPrimaryDisplay().workArea.width,
        height: electronScreen.getPrimaryDisplay().workArea.height,
        show: false,
        backgroundColor: 'white',
        webPreferences: {
            nodeIntegration: false,
            devTools: isDev,
            preload: path.join(__dirname, 'preload.js')
        }
    })
    const startURL = isDev ? 'http://localhost:3000' : `file://${path.join(__dirname, '../dist/index.html')}`

    mainWindow.loadURL(startURL)
    mainWindow.once('ready-to-show', () => mainWindow!.show())
    mainWindow.on('closed', () => {
        mainWindow = null
    })
    mainWindow.on('new-window-for-tab', (event: Electron.Event, url: string) => {
        event.preventDefault()
        mainWindow!.loadURL(url)
    })
}

app.whenReady().then(() => {

    app.on('activate', () => {
        if (!BrowserWindow.getAllWindows().length) {
            createMainWindow()
        }
    })
})

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit()
    }
})
