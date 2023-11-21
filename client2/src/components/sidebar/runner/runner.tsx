import React, { useState } from 'react'
import { useAppContext } from '../../../app-context'
import { useScaffold, ScaffoldState } from './scaffold'
import { Button } from '../../button'

export const RunnerPage: React.FC = () => {
    const context = useAppContext()
    const [scaffoldState, scaffold, manuallySetupScaffold, scaffoldLoading] = useScaffold()

    const [teamA, setTeamA] = useState<string | undefined>(undefined)
    const [teamB, setTeamB] = useState<string | undefined>(undefined)
    const [maps, setMaps] = useState<Set<string>>(new Set())

    const [availableTeams, setAvailableTeams] = useState<string[]>([])
    const [availableMaps, setAvailableMaps] = useState<string[]>([])

    const runGame = () => {
        if (!teamA || !teamB) return
        if (maps.size === 0) return
        if (!scaffold) return

        scaffold.runMatch(
            teamA,
            teamB,
            [...maps],
            () => {},
            () => {},
            () => {},
            () => {},
            () => {}
        )
    }

    // if instance of non-electron scaffold, then we need to connect to the server
    if (scaffoldState === ScaffoldState.NonElectron) return <>Run the client locally to use the runner</>

    return (
        <div className={'flex flex-col ' + scaffoldLoading ? 'opacity-50 pointer-events-none' : ''}>
            {scaffoldState === ScaffoldState.Disconnected || !scaffold ? (
                <>
                    <Button onClick={manuallySetupScaffold}>Setup Scaffold</Button>
                </>
            ) : (
                <>
                    <TeamSelector team={teamA} options={availableTeams} onChange={(t) => setTeamA(t)} />
                    <TeamSelector team={teamB} options={availableTeams} onChange={(t) => setTeamB(t)} />
                    <MapSelector
                        maps={maps}
                        availableMaps={availableMaps}
                        onSelect={(m) => setMaps((prev) => new Set([...prev, m]))}
                        onDeselect={(m) => setMaps((prev) => new Set([...prev].filter((x) => x !== m)))}
                    />

                    {!scaffold.running ? (
                        <Button onClick={runGame}>Run Game</Button>
                    ) : (
                        <Button onClick={scaffold.killMatch}>Kill Game</Button>
                    )}

                    <Console />
                </>
            )}
        </div>
    )
}

const TeamSelector: React.FC<{ team: string | undefined; options: string[]; onChange: (team: string) => void }> = ({
    team,
    options,
    onChange
}) => {
    return (
        <div className="flex flex-col">
            <label>Team</label>
            <select value={team} onChange={(e) => onChange(e.target.value)}>
                {options.map((t) => (
                    <option key={t} value={t}>
                        {t}
                    </option>
                ))}
            </select>
        </div>
    )
}

const MapSelector: React.FC<{
    maps: Set<string>
    availableMaps: string[]
    onSelect: (map: string) => void
    onDeselect: (map: string) => void
}> = ({ maps, availableMaps, onSelect, onDeselect }) => {
    return (
        <div className="flex flex-col">
            <label>Maps</label>
            <div className="flex flex-row">
                {availableMaps.map((m) => (
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

const Console: React.FC = () => {
    return (
        <div className="flex flex-col">
            <label>Console</label>
            <textarea className="flex-grow" />
        </div>
    )
}
