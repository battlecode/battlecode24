import React from 'react'
import { useAppContext } from './app-context'

export enum CanvasType {
    BACKGROUND = 'bkg',
    DYNAMIC = 'dyn',
    OVERLAY = 'ovly'
}

export const GameRenderer: React.FC = () => {
    const wrapperRef = React.useRef(null)
    const appContext = useAppContext()
    const [canvases, setCanvases] = React.useState(null as Map<CanvasType, React.ReactNode> | null)

    React.useEffect(() => {
        const canv = new Map<CanvasType, React.ReactNode>()
        for (const type in CanvasType) {
            canv.set(
                type as any as CanvasType,
                <canvas
                    className="absolute top-0 left-1/2"
                    style={{ transform: 'translate(-50%, -50%)' }}
                    key={`canv${type}`}
                />
            )
        }
        setCanvases(canv)
    }, [])

    React.useEffect(() => {
        const game = appContext.state.activeGame
        if (!game) return
        const match = game.currentMatch
        if (!match) return
        const interval = setInterval(() => {
            match.currentTurn.map
        })
        return () => clearInterval(interval)
    }, [canvases, appContext.state.activeGame])

    if (!canvases) return <></>

    return (
        <div className="w-full h-screen flex items-center justify-center">
            <div ref={wrapperRef} className="relative">
                {Array.from(canvases.values())}
            </div>
        </div>
    )
}

/*
position: absolute;
top: 0;
left: 50%;
transform: translateX(-50%);
*/
