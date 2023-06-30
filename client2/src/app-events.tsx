import { useEffect } from 'react'

export enum EventType {
    TURN_PROGRESS = 'turnprogress',
    RENDER = 'render'
}

export const useListenEvent = (eventType: EventType, callback: (event: any) => void, dependencies: any[] = []) => {
    useEffect(() => {
        document.addEventListener(eventType as string, callback)
        return () => {
            document.removeEventListener(eventType as string, callback)
        }
    }, dependencies)
}

export const publishEvent = (eventType: string, eventData: any) => {
    const event = new CustomEvent(eventType as string, eventData)
    document.dispatchEvent(event)
}
