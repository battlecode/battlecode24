import { useEffect, useState, useCallback } from "react";

// causes a component to rerender every intervalMillis milliseconds
// useful for stats components which don't want to subscribe to turn changes
// but just update every second
export function useRefresh(intervalMillis: number) {
    const [_, setTime] = useState(Date.now());
    useEffect(() => {
        const interval = setInterval(() => setTime(Date.now()), intervalMillis);
        return () => {
            clearInterval(interval);
        };
    }, []);
}

// returns a function that can be used to force a component to rerender
// useful for stats components to reread the contents
export function useForceUpdate(): () => void {
    const [, updateState] = useState({});
    const forceUpdate = useCallback(() => updateState({}), []);
    return forceUpdate
}
