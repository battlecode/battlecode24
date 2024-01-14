import { useEffect, useState } from 'react'

interface KeyState {
    keyCode: string
    repeat: boolean
    targetElem: EventTarget | null
    ctrlKey: boolean
    metaKey: boolean
    shiftKey: boolean
}

const DEFAULT_KEY_STATE: KeyState = {
    keyCode: '',
    repeat: false,
    targetElem: null,
    ctrlKey: false,
    metaKey: false,
    shiftKey: false
}

export function useKeyboard() {
    const [key, setKey] = useState<KeyState>(DEFAULT_KEY_STATE)

    useEffect(() => {
        const pressedCallback = (e: KeyboardEvent) =>
            setKey({
                keyCode: e.code,
                repeat: e.repeat,
                targetElem: e.target,
                ctrlKey: e.ctrlKey,
                metaKey: e.metaKey,
                shiftKey: e.shiftKey
            })
        const releasedCallback = (e: KeyboardEvent) => setKey(DEFAULT_KEY_STATE)

        window.addEventListener('keydown', pressedCallback)
        window.addEventListener('keyup', releasedCallback)

        return () => {
            window.removeEventListener('keydown', pressedCallback)
            window.removeEventListener('keyup', releasedCallback)
        }
    }, [])

    return key
}
