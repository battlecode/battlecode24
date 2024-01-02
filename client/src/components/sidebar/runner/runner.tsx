import React, { UIEvent, useEffect, useRef, useState } from 'react'
import { JavaInstall, useScaffold } from './scaffold'
import { Button, SmallButton } from '../../button'
import { nativeAPI } from './native-api-wrapper'
import { Select } from '../../forms'
import { InputDialog } from '../../input-dialog'
import Tooltip from '../../tooltip'

type RunnerPageProps = {
    scaffold: ReturnType<typeof useScaffold>
}

export const RunnerPage: React.FC<RunnerPageProps> = ({ scaffold }) => {
    const [
        setup,
        availableMaps,
        availablePlayers,
        javaInstalls,
        manuallySetupScaffold,
        scaffoldLoading,
        runMatch,
        killMatch,
        consoleLines
    ] = scaffold

    const getStoredInstalls = () => {
        const installs = JSON.parse(localStorage.getItem('customInstalls') ?? '[]') as string[]
        return installs.map((path) => ({
            display: path,
            path
        }))
    }

    const getDefaultInstall = () => {
        const install = localStorage.getItem('defaultInstall') ?? ''
        const storedInstalls = getStoredInstalls()
        return [...javaInstalls, ...storedInstalls].find((i) => i.path == install)
    }

    const [customInstalls, setCustomInstalls] = useState<JavaInstall[]>(getStoredInstalls())
    const [java, setJava] = useState<JavaInstall | undefined>(getDefaultInstall())
    const [teamA, setTeamA] = useState<string | undefined>(undefined)
    const [teamB, setTeamB] = useState<string | undefined>(undefined)
    const [maps, setMaps] = useState<Set<string>>(new Set())

    useEffect(() => {
        if (availablePlayers.size > 0) setTeamA([...availablePlayers][0])
        if (availablePlayers.size > 1) setTeamB([...availablePlayers][1])
    }, [availablePlayers])

    const runGame = () => {
        if (!teamA || !teamB || maps.size === 0 || !runMatch) return
        const javaPath = java ? java.path : javaInstalls.length > 0 ? javaInstalls[0].path : ''
        runMatch(javaPath, teamA, teamB, maps)
    }

    // if instance of non-electron scaffold, then we need to connect to the server
    if (!nativeAPI) return <>Run the client locally to use the runner</>

    const runDisabled = !teamA || !teamB || maps.size === 0
    return (
        <div className={'flex flex-col grow ' + (scaffoldLoading ? 'opacity-50 pointer-events-none' : '')}>
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
                    <JavaSelector
                        java={java}
                        javaInstalls={[...javaInstalls, ...customInstalls]}
                        onSelect={(j) => {
                            setJava(j)
                            localStorage.setItem('defaultInstall', j ? j.path : '')
                        }}
                        onAddCustom={(c) => {
                            const newInstalls = [...customInstalls, c]
                            setCustomInstalls(newInstalls)
                            localStorage.setItem('customInstalls', JSON.stringify(newInstalls.map((i) => i.path)))
                        }}
                    />

                    {!killMatch ? (
                        <div className="w-fit mx-auto">
                            <Tooltip
                                location="bottom"
                                text={runDisabled ? 'Please select both teams and a map' : 'Run the game'}
                            >
                                <Button className="mt-2" onClick={runGame} disabled={runDisabled}>
                                    Run Game
                                </Button>
                            </Tooltip>
                        </div>
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

interface JavaSelectorProps {
    java: JavaInstall | undefined
    javaInstalls: JavaInstall[]
    onSelect: (install: JavaInstall | undefined) => void
    onAddCustom: (install: JavaInstall) => void
}

const JavaSelector: React.FC<JavaSelectorProps> = (props) => {
    const [selectPath, setSelectPath] = React.useState(false)

    const closeDialog = (path: string) => {
        if (path) {
            const newJava = { display: path, path }
            props.onAddCustom(newJava)
            props.onSelect(newJava)
        }
        setSelectPath(false)
    }

    return (
        <>
            <div className="flex flex-col flex-grow mt-3">
                <label>Java Instance</label>
                <Select
                    className="w-full"
                    value={props.java ? props.java.path : ''}
                    onChange={(e) => {
                        if (e == '') {
                            props.onSelect(undefined)
                        } else if (e == 'CUSTOM') {
                            setSelectPath(true)
                        } else {
                            props.onSelect(props.javaInstalls.find((j) => j.path == e)!)
                        }
                    }}
                >
                    <option value={''}>Auto</option>
                    <option value={'CUSTOM'}>Custom</option>
                    {props.javaInstalls.map((t) => (
                        <option key={t.path} value={t.path}>
                            {t.display}
                        </option>
                    ))}
                </Select>
            </div>
            <InputDialog
                open={selectPath}
                onClose={closeDialog}
                title="Custom Java Path"
                description="Enter the Java path (should end with /Home)"
                placeholder="Path..."
            />
        </>
    )
}

export type ConsoleLine = { content: string; type: 'output' | 'error' | 'bold' }

type Props = {
    lines: ConsoleLine[]
}

export const Console: React.FC<Props> = ({ lines }) => {
    const consoleRef = useRef<HTMLDivElement>(null)

    const [tail, setTail] = useState(true)

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

    const handleScroll = (e: UIEvent<HTMLDivElement>) => {
        const div = e.currentTarget
        const isScrolledToBottom = div.scrollTop + div.offsetHeight - div.scrollHeight >= -10
        setTail(isScrolledToBottom)
    }

    useEffect(() => {
        if (lines.length == 0) setTail(true)
        if (tail && consoleRef.current) {
            consoleRef.current.scrollTop = consoleRef.current.scrollHeight
            setTail(true)
        }
    }, [lines])

    return (
        <div className="flex flex-col grow">
            <label>Console</label>
            <div
                ref={consoleRef}
                onScroll={handleScroll}
                className="flex-grow border border-black py-1 px-1 rounded-md overflow-auto flex flex-col"
                style={{ height: 'calc(50vh - 150px)' }}
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
