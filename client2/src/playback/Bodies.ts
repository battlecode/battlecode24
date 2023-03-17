export default class Bodies {
    // This needs to use struct of arrays stuff, maybe use the existing implementation? or we could hardcode the rows right here, maybe.
}

type BodiesRow = {
    id: number;
    x: number;
    y: number;
    type: number;
};

type Body = {
    name: string;
    draw(row: BodiesRow, ctx: CanvasRenderingContext2D): void;
    onHoverInfo(row: BodiesRow): string;
};

export const bodiesMap: Record<number, Body> = {
    1: {
        name: 'Archon',
        draw(row: BodiesRow, ctx: CanvasRenderingContext2D): void {
        },
        onHoverInfo(row: BodiesRow): string {
            return 'Archon';
        }
    },
    2: {
        name: 'Launcher',
        draw(row: BodiesRow, ctx: CanvasRenderingContext2D): void {
        },
        onHoverInfo(row: BodiesRow): string {
            return 'Launcher';
        }
    },
};