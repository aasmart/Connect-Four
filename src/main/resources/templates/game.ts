const SOCKET_ROUTE_URI = "ws://127.0.0.1:8080/api/game"
const API_ROUTE_URI = "http://127.0.0.1:8080/api/game"

let client: WebSocket;
let player: PlayerData;
let lastState: GameState

window.addEventListener("load", () => {
    // @ts-ignore
    const GAME_TILES = Array.from(document.getElementsByClassName("gameTile"));

    const location = window.location.href;
    const splitLocation = location.split("/");

    const gameId = splitLocation[splitLocation.length - 1];

    client = new WebSocket(`${SOCKET_ROUTE_URI}/${gameId}`);

    client.onopen = () => {
        console.log("Connected")

        fetch(`${API_ROUTE_URI}/${gameId}/player-data`).then(res => {
            if(!res.ok)
                throw new Error();

            return res.json();
        }).then(data => {
              player = JSON.parse(JSON.stringify(data));
              if(lastState)
                  handleGameState(GAME_TILES, lastState)
        }).catch(err => {
            console.log(err);
        });
    }

    client.onmessage = (e) => {
        try {
            lastState = JSON.parse(e.data)
            if(player)
                handleGameState(GAME_TILES, lastState)
        } catch (e) {
            console.log(e)
        }
    }
});

function getTitleString(state: GameState) {
    let title
    switch(state.gameStatus) {
        case GameStatus.ACTIVE:
            if(state.isPlayerOneTurn)
                if(player.playerRole === "PLAYER_ONE")
                    title = "It's your turn!";
                else
                    title = "It's Player 1's turn!"
            else if(!state.isPlayerOneTurn)
                if(player.playerRole === "PLAYER_TWO")
                    title = "It's your turn!";
                else
                    title = "It's Player 2's turn!"
            break;
        case GameStatus.PLAYER_ONE_WON:
            if(player.playerRole === "PLAYER_ONE")
                title = "You won!"
            else
                title = "Player One wins!"
            break;
        case GameStatus.PLAYER_TWO_WON:
            if(player.playerRole === "PLAYER_TWO")
                title = "You won!"
            else
                title = "Player Two wins!"
            break;
        case GameStatus.DRAWN:
            title = "The game has ended in a draw"
            break;
        case GameStatus.WAITING_FOR_PLAYERS:
            title = "Waiting for players to join"
            break;
        default:
            title = "Unknown state"
            break;
    }

    return title
}

function handleGameState(tiles: Element[], state: GameState) {
    tiles.forEach((tile, index) => {
        const tileState = state.gameTiles[index];
        const pieceType = PieceType[tileState.pieceType]

        if(tileState.pieceType != PieceType.EMPTY) {
            tile.classList.add("fall")
            tile.classList.add(pieceType.toLowerCase());
        }

        const canPlay =
            state.isPlayerOneTurn && player.playerRole === "PLAYER_ONE" ||
            !state.isPlayerOneTurn && player.playerRole === "PLAYER_TWO";

        tile.toggleAttribute("disabled", (!tileState.canPlace || !canPlay));
        tile.classList.toggle("canPlace", tileState.canPlace && canPlay);

        document.getElementById("state-title").innerText = getTitleString(state);
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
    ACTIVE,
    DRAWN,
    WON,
    PLAYER_ONE_WON,
    PLAYER_TWO_WON,
    WAITING_FOR_PLAYERS
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
    isPlayerOneTurn: Boolean
    gameStatus: GameStatus
}

interface PlayerData {
    playerRole: string
}