import { useEffect, useState } from "react"

export function useKeyboard() {
	const [key, setKey] = useState({ keyCode: "", repeat: false });

    useEffect(() => {
		const pressedCallback = (e: KeyboardEvent) => setKey({ keyCode: e.code, repeat: e.repeat });
		const releasedCallback = (e: KeyboardEvent) => setKey({ keyCode: "", repeat: e.repeat });

        window.addEventListener("keydown", pressedCallback);
        window.addEventListener("keyup", releasedCallback);

        return () => {
            window.removeEventListener("keydown", pressedCallback);
            window.removeEventListener("keyup", releasedCallback);
        };
    }, []);

    return key;
};
