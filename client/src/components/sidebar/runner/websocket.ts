import { schema, flatbuffers } from 'battlecode-schema'
import Game from '../../../playback/Game'
import Match from '../../../playback/Match'
import assert from 'assert'
import { EventType, publishEvent } from '../../../app-events'

export type FakeGameWrapper = {
    events: (index: number, unusedEventSlot: any) => schema.EventWrapper | null
    eventsLength: () => number
}

export default class WebSocketListener {
    url: string = 'ws://localhost:6175'
    pollEvery: number = 500
    activeGame: Game | null = null
    constructor(
        readonly onGameCreated: (game: Game) => void,
        readonly onMatchCreated: (match: Match) => void,
        readonly onGameComplete: () => void
    ) {
        this.poll()
    }

    private reset() {
        this.activeGame = null
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
            this.reset()
        }
        ws.onclose = (event) => {
            this.reset()
            window.setTimeout(() => {
                this.poll()
            }, this.pollEvery)
        }
    }

    private handleEvent(data: ArrayBuffer) {
        const event = schema.EventWrapper.getRootAsEventWrapper(new flatbuffers.ByteBuffer(new Uint8Array(data)))
        const eventType = event.eType()

        if (this.activeGame === null) {
            assert(eventType === schema.Event.GameHeader, 'First event must be GameHeader')
            this.sendInitialGame(event)
            return
        }

        this.activeGame.addEvent(event)

        switch (eventType) {
            case schema.Event.MatchHeader: {
                const match = this.activeGame.matches[this.activeGame.matches.length - 1]
                this.sendInitialMatch(match)
                break
            }
            case schema.Event.Round: {
                const match = this.activeGame.matches[this.activeGame.matches.length - 1]
                // Auto progress the turn if the user hasn't done it themselves
                if (match.currentTurn.turnNumber == match.maxTurn - 1) {
                    match.jumpToEnd(true)
                } else {
                    // Publish anyways so the control bar updates
                    publishEvent(EventType.TURN_PROGRESS, {})
                }
                break
            }
            case schema.Event.GameFooter: {
                this.sendCompleteGame()
                break
            }
            default:
                break
        }
    }

    private sendInitialGame(headerEvent: schema.EventWrapper) {
        const fakeGameWrapper: FakeGameWrapper = {
            events: () => headerEvent,
            eventsLength: () => 1
        }

        this.activeGame = new Game(fakeGameWrapper)

        this.onGameCreated(this.activeGame)
    }

    private sendInitialMatch(match: Match) {
        this.onMatchCreated(match)
    }

    private sendCompleteGame() {
        this.onGameComplete()
        this.reset()
    }
}
