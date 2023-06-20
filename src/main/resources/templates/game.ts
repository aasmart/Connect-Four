const SOCKET_ROUTE_URI = "ws://127.0.0.1:8080/api/game"

let client: WebSocket;

window.addEventListener("load", () => {
    // @ts-ignore
    const GAME_TILES = Array.from(document.getElementsByClassName("gameTile"));

    const location = window.location.href;
    const splitLocation = location.split("/");

    const gameId = splitLocation[splitLocation.length - 1];

    client = new WebSocket(`${SOCKET_ROUTE_URI}/${gameId}`);

    client.onopen = () => {
        console.log("Connected")
    }

    client.onmessage = (e) => {
        try {
            handleGameState(GAME_TILES, JSON.parse(e.data))
        } catch (e) {
            console.log(e)
        }
    }
});

function handleGameState(tiles: Element[], state: Packet) {
    tiles[state.placeIndex].classList.toggle(PieceType[state.placePieceType].toLowerCase())
}

function placePiece(index: number) {
    const packet: Packet = {
        placePieceType: PieceType.RED,
        placeIndex: index
    }

    client.send(JSON.stringify(packet))
}

enum PieceType {
    RED,
    YELLOW,
    EMPTY
}
interface Packet {
    placePieceType: PieceType,
    placeIndex: number
}