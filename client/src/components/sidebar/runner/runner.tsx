import React, { useEffect, useRef, useState } from 'react'
import { useScaffold } from './scaffold'
import { Button, SmallButton } from '../../button'
import { nativeAPI } from './native-api-wrapper'
import { Select } from '../../forms'

type RunnerPageProps = {
    scaffold: ReturnType<typeof useScaffold>
}

export const RunnerPage: React.FC<RunnerPageProps> = ({ scaffold }) => {
    const [
        setup,
        availableMaps,
        availablePlayers,
        manuallySetupScaffold,
        scaffoldLoading,
        runMatch,
        killMatch,
        consoleLines
    ] = scaffold

    const [teamA, setTeamA] = useState<string | undefined>(undefined)
    const [teamB, setTeamB] = useState<string | undefined>(undefined)
    const [maps, setMaps] = useState<Set<string>>(new Set())

    useEffect(() => {
        if (availablePlayers.size > 0) setTeamA([...availablePlayers][0])
        if (availablePlayers.size > 1) setTeamB([...availablePlayers][1])
    }, [availablePlayers])

    const runGame = () => {
        if (!teamA || !teamB || maps.size === 0 || !runMatch) return
        runMatch(teamA, teamB, maps)
    }

    // if instance of non-electron scaffold, then we need to connect to the server
    // if (!nativeAPI) return <>Run the client locally to use the runner</>

    return (
        <div className={'flex flex-col ' + (scaffoldLoading ? 'opacity-50 pointer-events-none' : '')}>
            {!setup ? (
                <>
                    <Button onClick={manuallySetupScaffold}>Setup Scaffold</Button>
                </>
            ) : (
                <>
                    <TeamSelector
                        teamA={teamA}
                        teamB={teamB}
                        options={availablePlayers}
                        onChangeA={(t) => setTeamA(t)}
                        onChangeB={(t) => setTeamB(t)}
                    />
                    <MapSelector
                        maps={maps}
                        availableMaps={availableMaps}
                        onSelect={(m) => setMaps(new Set([...maps, m]))}
                        onDeselect={(m) => setMaps(new Set([...maps].filter((x) => x !== m)))}
                    />

                    {!killMatch ? (
                        <Button onClick={runGame} disabled={!teamA || !teamB || maps.size === 0}>
                            Run Game
                        </Button>
                    ) : (
                        <Button onClick={killMatch}>Kill Game</Button>
                    )}

                    <Console lines={consoleLines} />
                    <SmallButton className="mt-2" onClick={manuallySetupScaffold}>
                        Re-configure Scaffold
                    </SmallButton>
                </>
            )}
        </div>
    )
}

interface TeamSelectorProps {
    teamA: string | undefined
    teamB: string | undefined
    options: Set<string>
    onChangeA: (team: string | undefined) => void
    onChangeB: (team: string | undefined) => void
}

const TeamSelector: React.FC<TeamSelectorProps> = ({ teamA, teamB, options, onChangeA, onChangeB }) => {
    return (
        <div className="flex flex-row">
            <div className="flex flex-col flex-grow">
                <label>Team A</label>
                <Select className="w-full" value={teamA} onChange={(e) => onChangeA(e)}>
                    {teamA === undefined && <option value={undefined}>Select a team</option>}
                    {[...options].map((t) => (
                        <option key={t} value={t}>
                            {t}
                        </option>
                    ))}
                </Select>
            </div>
            <div className="flex flex-col justify-center">
                <div
                    onClick={() => {
                        const tmp = teamB
                        onChangeB(teamA)
                        onChangeA(tmp)
                    }}
                    className="mx-2 whitespace-nowrap cursor-pointer mt-auto mb-2 text-xs font-bold"
                >
                    {'<->'}
                </div>
            </div>
            <div className="flex flex-col flex-grow">
                <label className="ml-auto">Team B</label>
                <Select className="w-full" value={teamB} onChange={(e) => onChangeB(e)}>
                    {teamB === undefined && <option value={undefined}>Select a team</option>}
                    {[...options].map((t) => (
                        <option key={t} value={t}>
                            {t}
                        </option>
                    ))}
                </Select>
            </div>
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
        <div className="flex flex-col mt-3">
            <label>Maps</label>
            <div className="flex flex-col border border-black py-1 px-1 rounded-md h-200 overflow-y-auto">
                {[...availableMaps].map((m) => (
                    <div
                        key={m}
                        className={(maps.has(m) && 'bg-gray-200') + ' cursor-pointer hover:bg-gray-200'}
                        onClick={() => (maps.has(m) ? onDeselect(m) : onSelect(m))}
                    >
                        {m}
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

    const consoleRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        if (consoleRef.current) {
            const { scrollHeight, clientHeight, scrollTop } = consoleRef.current
            const isScrolledToBottom = scrollHeight - clientHeight <= scrollTop + 40

            if (isScrolledToBottom) {
                consoleRef.current.scrollTop = consoleRef.current.scrollHeight
            }
        }
    }, [lines])

    return (
        <div className="flex flex-col">
            <label>Console</label>
            <div
                ref={consoleRef}
                className="flex-grow border border-black py-1 px-1 rounded-md max-h-[600px] overflow-auto min-h-[200px] flex flex-col"
            >
                {lines.map((line, index) => (
                    <span key={index} className={getLineClass(line) + ' text-xs whitespace-nowrap'}>
                        {line.content}
                    </span>
                ))}
            </div>
        </div>
    )
}
