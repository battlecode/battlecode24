import React, { useState } from 'react'
import { useAppContext } from '../../app-context'
import { BATTLECODE_YEAR } from '../../constants'

export const QueuePage: React.FC = () => {
    const context = useAppContext()
    const queue = context.state.queue

    const onUpload = () => {
        context.setState({
            ...context.state,
            queue: queue.concat(0)
        })
    }

    return (
        <div className="flex flex-col">
            <button
                className="text-xs m-2 p-2 bg-button rounded-md"
                onClick={onUpload}
            >
                    Upload a .bc{BATTLECODE_YEAR % 100} replay file
            </button>
            <p>Games ({queue.length === 0 ? 0 : 1}/{queue.length})</p>
            {
                queue.map((game) => <div>
                    {game}
                </div>)
            }
        </div>
    )
}
