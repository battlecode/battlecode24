const IMAGE_DIRECTORY = 'static/img/';

const imgCache = new Map<string, Promise<HTMLImageElement>>();

export async function loadImage(path: string): Promise<HTMLImageElement> {
    if (imgCache.has(path))
        return await imgCache.get(path)!;

    const img = new Image();
    img.src = IMAGE_DIRECTORY + path;

    const loading = new Promise<HTMLImageElement>((resolve, reject) => {
        img.onload = () => resolve(img);
        imgCache.set(path, loading);
        img.onerror = reject;
    });

    imgCache.set(path, loading);
    return loading;
}