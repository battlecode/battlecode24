import { useEffect, useState } from 'react'

export function useKeyboard() {
    const [key, setKey] = useState<{ keyCode: string; repeat: boolean; targetElem: EventTarget | null }>({
        keyCode: '',
        repeat: false,
        targetElem: null
    })

    useEffect(() => {
        const pressedCallback = (e: KeyboardEvent) =>
            setKey({ keyCode: e.code, repeat: e.repeat, targetElem: e.target })
        const releasedCallback = (e: KeyboardEvent) => setKey({ keyCode: '', repeat: e.repeat, targetElem: e.target })

        window.addEventListener('keydown', pressedCallback)
        window.addEventListener('keyup', releasedCallback)

        return () => {
            window.removeEventListener('keydown', pressedCallback)
            window.removeEventListener('keyup', releasedCallback)
        }
    }, [])

    return key
}
