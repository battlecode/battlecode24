import React, { useEffect } from 'react'
import { useAppContext } from './app-context'
import { EventType, publishEvent } from './app-events'

const DEFAULT_CONFIG = {
    showAllIndicators: false,
    showAllRobotRadii: false,
    showHealthBars: false
}

export type ClientConfig = typeof DEFAULT_CONFIG

const configDescription: { [key: string]: string } = {
    showAllIndicators: 'Show all indicator dots and lines',
    showAllRobotRadii: 'Show all robot view and attack radii',
    showHealthBars: 'Show health bars below all robots'
}

export function getDefaultConfig(): ClientConfig {
    const config: ClientConfig = { ...DEFAULT_CONFIG }
    for (const key in config) {
        const value = localStorage.getItem('config' + key)
        if (value) config[key as keyof ClientConfig] = JSON.parse(value)
    }
    return config
}

export const ConfigPage = () => {
    return (
        <div className={'flex flex-col'}>
            <div className="mb-2">Edit Client Config:</div>
            {Object.entries(DEFAULT_CONFIG).map(([key, value]) => {
                if (typeof value === 'string') return <ConfigStringElement configKey={key} key={key} />
                if (typeof value === 'boolean') return <ConfigBooleanElement configKey={key} key={key} />
                if (typeof value === 'number') return <ConfigNumberElement configKey={key} key={key} />
            })}
        </div>
    )
}

const ConfigBooleanElement: React.FC<{ configKey: string }> = ({ configKey }) => {
    const context = useAppContext()
    const value = context.state.config[configKey as keyof ClientConfig]
    return (
        <div className={'flex flex-row items-center mb-2'}>
            <input
                type={'checkbox'}
                checked={value}
                onChange={(e) => {
                    context.setState({
                        ...context.state,
                        config: { ...context.state.config, [configKey]: e.target.checked }
                    })
                    localStorage.setItem('config' + configKey, JSON.stringify(e.target.checked))
                    // hopefully after the setState is done
                    setTimeout(() => publishEvent(EventType.RENDER, {}), 10)
                }}
            />
            <div className={'ml-2 text-xs'}>{configDescription[configKey] ?? configKey}</div>
        </div>
    )
}

const ConfigStringElement: React.FC<{ configKey: string }> = ({ configKey }) => {
    const context = useAppContext()
    const value = context.state.config[configKey as keyof ClientConfig]
    return <div className={'flex flex-row items-center'}>Todo</div>
}

const ConfigNumberElement: React.FC<{ configKey: string }> = ({ configKey }) => {
    const context = useAppContext()
    const value = context.state.config[configKey as keyof ClientConfig]
    return <div className={'flex flex-row items-center'}>Todo</div>
}
