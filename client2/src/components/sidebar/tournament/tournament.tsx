import React from 'react'
import { useAppContext } from '../../../app-context'
import { Button } from '../../button'
import { FiUpload } from 'react-icons/fi'
import Tournament, { JsonTournamentGame } from '../../../playback/Tournament'
import { useSearchParamString } from '../../../app-search-params'

interface TournamentPageProps {
    loadingRemoteTournament: boolean
}

export const TournamentPage: React.FC<TournamentPageProps> = ({ loadingRemoteTournament }) => {
    const context = useAppContext()
    const inputRef = React.useRef<HTMLInputElement | null>()

    const upload = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files || e.target.files.length == 0) return
        const file = e.target.files[0]
        const reader = new FileReader()
        reader.onload = () => {
            context.setState({
                ...context.state,
                tournament: new Tournament(JSON.parse(reader.result as string) as JsonTournamentGame[])
            })
        }
        reader.readAsText(file)
    }

    return loadingRemoteTournament ? (
        <div>Loading tournament...</div>
    ) : (
        <div className={'flex flex-col'}>
            <input type="file" hidden ref={(ref) => (inputRef.current = ref)} onChange={upload} accept={'.json'} />
            <Button onClick={loadingRemoteTournament ? () => {} : () => inputRef.current?.click()}>
                <FiUpload className="align-middle text-base mr-2" />
                Upload a tournament file
            </Button>
        </div>
    )
}
