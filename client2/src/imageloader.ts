const IMAGE_DIRECTORY = 'static/img/'

const imgCache = new Map<string, Promise<HTMLImageElement>>()
const loadedImgCache = new Map<string, HTMLImageElement>()
/**
 * @param path the path of the image to load
 * @returns a promise that resolves to the image once the image has been loaded
 */
export async function loadImage(path: string): Promise<HTMLImageElement> {
    if (imgCache.has(path)) return await imgCache.get(path)!

    const img = new Image()
    img.src = IMAGE_DIRECTORY + path

    let loading = new Promise<HTMLImageElement>(() => {})
    loading = new Promise<HTMLImageElement>((resolve, reject) => {
        img.onload = () => {
            loadedImgCache.set(path, img)
            resolve(img)
        }
        imgCache.set(path, loading)
        img.onerror = reject
    })
    loading.catch((e) => {
        throw new Error(`Failed to load image ${path}: ${e}`)
    })

    imgCache.set(path, loading)
    return loading
}

/**
 * Returns an image if it has been loaded, or undefined.
 * Starts loading the image if it has not been loaded already
 *
 * @param path the path of the image to load
 * @returns the image if it has already been loaded, or undefined otherwise
 */
export function getImageIfLoaded(path: string) {
    if (loadedImgCache.has(path)) return loadedImgCache.get(path)
    loadImage(path)
    return undefined
}
