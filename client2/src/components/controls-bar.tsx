import React from 'react'
import * as ControlIcons from '../icons/controls'
import { ControlsBarButton } from './controls-bar-button'

export const ControlsBar: React.FC = () => {
    return (
        <div className="flex bg-pink-900 fixed bottom-0 left-1/2 -translate-x-1/2 p-1.5 rounded-t-md text-white gap-1.5 shadow-centered">
            <div className="min-w-[350px] min-h-[30px] bg-gray-800 rounded-md mr-2 relative">
                <p className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-xs pointer-events-none">
                    Round: <b>0</b>/2000
                </p>
            </div>
            <ControlsBarButton icon={<ControlIcons.ReverseIcon />} tooltip="" />
            <ControlsBarButton icon={<ControlIcons.SkipBackwardsIcon />} tooltip="" />
            <ControlsBarButton icon={<ControlIcons.GoPreviousIcon />} tooltip="" />
            <ControlsBarButton icon={<ControlIcons.PlaybackPauseIcon />} tooltip="" />
            <ControlsBarButton icon={<ControlIcons.GoNextIcon />} tooltip="" />
            <ControlsBarButton icon={<ControlIcons.SkipForwardsIcon />} tooltip="" />
            <ControlsBarButton icon={<ControlIcons.PlaybackStopIcon />} tooltip="" />
            <ControlsBarButton icon={<ControlIcons.GoEndIcon />} tooltip="" />
        </div>
    )
}
