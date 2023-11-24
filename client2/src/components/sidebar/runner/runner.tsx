import React, { useState } from 'react'
import { useScaffold } from './scaffold'
import { Button } from '../../button'
import { nativeAPI } from './native-api-wrapper'

export const RunnerPage: React.FC = () => {
    const [
        setup,
        availableMaps,
        availablePlayers,
        manuallySetupScaffold,
        scaffoldLoading,
        runMatch,
        killMatch,
        console
    ] = useScaffold()

    const [teamA, setTeamA] = useState<string | undefined>(undefined)
    const [teamB, setTeamB] = useState<string | undefined>(undefined)
    const [maps, setMaps] = useState<Set<string>>(new Set())

    const runGame = () => {
        if (!teamA || !teamB || maps.size === 0 || !runMatch) return
        runMatch(teamA, teamB, maps)
    }

    // if instance of non-electron scaffold, then we need to connect to the server
    if (!nativeAPI) return <>Run the client locally to use the runner</>

    return (
        <div className={'flex flex-col ' + scaffoldLoading ? 'opacity-50 pointer-events-none' : ''}>
            {!setup ? (
                <>
                    <Button onClick={manuallySetupScaffold}>Setup Scaffold</Button>
                </>
            ) : (
                <>
                    <TeamSelector team={teamA} options={availablePlayers} onChange={(t) => setTeamA(t)} />
                    <TeamSelector team={teamB} options={availablePlayers} onChange={(t) => setTeamB(t)} />
                    <MapSelector
                        maps={maps}
                        availableMaps={availableMaps}
                        onSelect={(m) => setMaps((prev) => new Set([...prev, m]))}
                        onDeselect={(m) => setMaps((prev) => new Set([...prev].filter((x) => x !== m)))}
                    />

                    {!killMatch ? (
                        <Button onClick={runGame} disabled={!teamA || !teamB || maps.size == 0}>
                            Run Game
                        </Button>
                    ) : (
                        <Button onClick={killMatch}>Kill Game</Button>
                    )}

                    <Console lines={console} />
                </>
            )}
        </div>
    )
}

interface TeamSelectorProps {
    team: string | undefined
    options: Set<string>
    onChange: (team: string) => void
}

const TeamSelector: React.FC<TeamSelectorProps> = ({ team, options, onChange }) => {
    return (
        <div className="flex flex-col">
            <label>Team</label>
            <select value={team} onChange={(e) => onChange(e.target.value)}>
                {[...options].map((t) => (
                    <option key={t} value={t}>
                        {t}
                    </option>
                ))}
            </select>
        </div>
    )
}

interface MapSelectorProps {
    maps: Set<string>
    availableMaps: Set<string>
    onSelect: (map: string) => void
    onDeselect: (map: string) => void
}

const MapSelector: React.FC<MapSelectorProps> = ({ maps, availableMaps, onSelect, onDeselect }) => {
    return (
        <div className="flex flex-col">
            <label>Maps</label>
            <div className="flex flex-row">
                {[...availableMaps].map((m) => (
                    <div key={m} className="flex flex-col">
                        <input
                            type="checkbox"
                            checked={maps.has(m)}
                            onChange={(e) => (e.target.checked ? onSelect(m) : onDeselect(m))}
                        />
                        <label>{m}</label>
                    </div>
                ))}
            </div>
        </div>
    )
}

export type ConsoleLine = { content: string; type: 'output' | 'error' | 'bold' }

type Props = {
    lines: ConsoleLine[]
}

export const Console: React.FC<Props> = ({ lines }) => {
    const getLineClass = (line: ConsoleLine) => {
        switch (line.type) {
            case 'output':
                return ''
            case 'error':
                return 'text-red-500'
            case 'bold':
                return 'font-bold'
        }
    }

    return (
        <div className="flex flex-col">
            <label>Console</label>
            <textarea className="flex-grow">
                {lines.map((line, index) => (
                    <span key={index} className={getLineClass(line)}>
                        {line.content}
                    </span>
                ))}
            </textarea>
        </div>
    )
}
