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
        console.log(e.data)
        try {
            handleGameState(GAME_TILES, JSON.parse(e.data))
        } catch (e) {
            console.log(e)
        }
    }
});

function handleGameState(tiles: Element[], state: GameState) {
    tiles.forEach((tile, index) => {
        const tileState = state.gameTiles[index];
        const pieceType = PieceType[tileState.pieceType]

        if(tileState.pieceType != PieceType.EMPTY) {
            tile.classList.add("fall")
            tile.classList.add(pieceType.toLowerCase());
        }

        tile.toggleAttribute("disabled", !tileState.canPlace)
        tile.classList.toggle("canPlace", tileState.canPlace)

        let title
        if(state.gameStatus == GameStatus.ACTIVE) {
            if(state.isPlayerOneTurn)
                title = "It's Player 1's turn!";
            else if(!state.isPlayerOneTurn)
                title = "It's Player 2's turn!";
        } else if(state.gameStatus == GameStatus.WON) {
            if(state.isPlayerOneTurn)
                title = "Player 1 wins!";
            else
                title = "Player 2 wins!";
        } else
            title = "The game has ended in a draw!";

        document.getElementById("state-title").innerText = title;
    })
}

function placePiece(index: number) {
    const packet: Packet = {
        placeIndex: index
    }

    client.send(JSON.stringify(packet))
}

enum PieceType {
    RED,
    YELLOW,
    EMPTY
}

enum GameStatus {
    WON,
    DRAWN,
    ACTIVE
}

interface Packet {
    placeIndex: number
}

interface GameTile {
    pieceType: number,
    canPlace: boolean
}

interface GameState {
    gameTiles: GameTile[]
    isTurn: boolean
    isPlayerOneTurn: Boolean
    gameStatus: GameStatus
}