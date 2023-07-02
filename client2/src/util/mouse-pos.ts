import { useEffect, useState } from "react"

export function useMousePosition() {
    const [position, setPosition] = useState({ x: 0, y: 0 });

    useEffect(() => {
        const callback = (e: MouseEvent) => setPosition({ x: e.clientX, y: e.clientY });
        window.addEventListener("mousemove", callback);

        return () => {
            window.removeEventListener("mousemove", callback);
        };
    }, []);

    return position;
};
