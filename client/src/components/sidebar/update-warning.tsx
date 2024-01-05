import React, { useEffect } from 'react'
import { BATTLECODE_YEAR, GAME_VERSION } from '../../constants'
import { nativeAPI } from './runner/native-api-wrapper'

export const UpdateWarning = () => {
    const [update, setUpdate] = React.useState<undefined | { latest: string }>(undefined)

    useEffect(() => {
        // Don't need to check for version updates in the web client
        if (!nativeAPI) return

        const url = `https://api.battlecode.org/api/episode/e/bc${BATTLECODE_YEAR % 100}/?format=json`
        fetch(url, { mode: 'no-cors' })
            .then((response) => response.json())
            .then((json) => {
                const latest = json.release_version_public
                if (latest.trim() != GAME_VERSION) {
                    setUpdate({ latest })
                }
            })
            .catch((e) => console.log(e))
    }, [])

    if (!update) return null

    return (
        <div className="bg-yellow-200 p-2 text-center">
            <p className="text-yellow-800 text-xs">
                <b>NEW VERSION AVAILABLE!</b>
                <br />
                download with <code>gradle update</code> followed by <code>gradle build</code>, and then restart the
                client: v{update.latest}
            </p>
        </div>
    )
}
