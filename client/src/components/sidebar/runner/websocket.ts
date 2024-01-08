import { schema, flatbuffers } from 'battlecode-schema'
import Game from '../../../playback/Game'

export type FakeGameWrapper = {
    events: (index: number, unusedEventSlot: any) => schema.EventWrapper | null
    eventsLength: () => number
}

export default class WebSocketListener {
    url: string = 'ws://localhost:6175'
    pollEvery: number = 500
    events: schema.EventWrapper[] = []
    constructor(readonly onGameComplete: (game: Game) => void) {
        this.poll()
    }

    private poll() {
        const ws = new WebSocket(this.url)
        ws.binaryType = 'arraybuffer'
        ws.onopen = (event) => {
            console.log(`Connected to ${this.url}`)
        }
        ws.onmessage = (event) => {
            this.handleEvent(<ArrayBuffer>event.data)
        }
        ws.onerror = (event) => {
            this.events = []
        }
        ws.onclose = (event) => {
            this.events = []
            window.setTimeout(() => {
                this.poll()
            }, this.pollEvery)
        }
    }

    private handleEvent(data: ArrayBuffer) {
        const event = schema.EventWrapper.getRootAsEventWrapper(new flatbuffers.ByteBuffer(new Uint8Array(data)))

        this.events.push(event)

        if (event.eType() === schema.Event.GameHeader) {
            if (this.events.length !== 1) throw new Error('GameHeader event must be first event')
        } else if (event.eType() === schema.Event.GameFooter) {
            if (this.events.length === 1) throw new Error('GameFooter event must be after GameHeader event')
            this.sendCompleteGame()
        }
    }

    private sendCompleteGame() {
        const fakeGameWrapper: FakeGameWrapper = {
            events: (index: number, unusedEventSlot: any) => {
                return this.events[index]
            },
            eventsLength: () => {
                return this.events.length
            }
        }

        this.onGameComplete(new Game(fakeGameWrapper))

        this.events = []
    }
}
