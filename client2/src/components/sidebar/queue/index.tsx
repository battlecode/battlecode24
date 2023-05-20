import React from 'react'
import { useAppContext } from '../../../app-context'
import { BATTLECODE_YEAR } from '../../../constants'
import Button from '../../button'
import { FiUpload } from 'react-icons/fi'

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
            <Button
                onClick={onUpload}
            >
                <FiUpload className="align-middle text-base mr-2"/>
                Upload a .bc{BATTLECODE_YEAR % 100} replay file
            </Button>
            <p>Games ({queue.length === 0 ? 0 : 1}/{queue.length})</p>
            {
                queue.map((game) => <div>
                    {game}
                </div>)
            }
        </div>
    )
}
