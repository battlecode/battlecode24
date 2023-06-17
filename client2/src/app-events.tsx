import { useEffect } from 'react'

export enum EventType {
    TURN_PROGRESS = "turnprogress",
}

export function useListenEvent(eventType: EventType, callback: (event: any) => void) {
    useEffect(() => {
        document.addEventListener(eventType as string, callback)
        return () => {
            document.removeEventListener(eventType as string, callback);
        }
    }, [])
}

export function publishEvent(eventType: string, eventData: any) {
    const event = new CustomEvent(eventType as string, eventData)
    document.dispatchEvent(event);
}
