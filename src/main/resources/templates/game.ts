const ip = "127.0.0.1:8080"
const SOCKET_ROUTE_URI = `ws://${ip}/api/game`
const API_ROUTE_URI = `http://${ip}/api/game`

let client: WebSocket;
let player: PlayerData;
let lastState: GameState;
let gameId: string;

const copySvgPromise = fetch("/static/copy.svg")
    .then(res => res.text())
    .then(text => new DOMParser().parseFromString(text, "text/xml"))
    .then(html => html.getElementsByTagName("svg")[0]);

window.addEventListener("load", () => {
    // @ts-ignore
    const GAME_TILES = Array.from(document.getElementsByClassName("gameTile"));

    const location = window.location.href;
    const splitLocation = location.split("/");

    gameId = splitLocation[splitLocation.length - 1];

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
        case GameStatus.PLAYER_DISCONNECTED:
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
            tile.classList.add("fall");
            tile.classList.add(pieceType.toLowerCase());
        } else {
            tile.classList.remove("red", "yellow", "fall");
        }

        const canPlay =
            state.isPlayerOneTurn && player.playerRole === "PLAYER_ONE" ||
            !state.isPlayerOneTurn && player.playerRole === "PLAYER_TWO";

        tile.toggleAttribute("disabled", (!tileState.canPlace || !canPlay));
        tile.classList.toggle("canPlace", tileState.canPlace && canPlay);

        document.getElementById("state-title").innerText = getTitleString(state);

        const replayButton = document.getElementById("play-again");

        if(state.playerOneRematch && player.playerRole == "PLAYER_ONE" ||
            state.playerTwoRematch && player.playerRole == "PLAYER_TWO"
        ) {
            replayButton.innerText = "Cancel Rematch Request";
            replayButton.toggleAttribute("disabled", false);
            replayButton.setAttribute("data-action", "destructive");
        } else if(state.playerOneRematch || state.playerTwoRematch) {
            replayButton.innerText = "Accept Rematch Request";
            replayButton.toggleAttribute("disabled", false);
            replayButton.setAttribute("data-action", "normal");
        } else {
            replayButton.innerText = "Request Rematch";
            replayButton.toggleAttribute(
                "disabled",
                state.gameStatus == GameStatus.ACTIVE || state.gameStatus == GameStatus.WAITING_FOR_PLAYERS
            );
            replayButton.setAttribute("data-action", "normal");
        }

        const modal: HTMLDialogElement = document.getElementById("loading") as HTMLDialogElement;
        if(state.gameStatus == GameStatus.WAITING_FOR_PLAYERS || state.gameStatus == GameStatus.PLAYER_DISCONNECTED) {
            const modalContent = modal.getElementsByTagName("p")[0];
            switch (state.gameStatus) {
                case GameStatus.WAITING_FOR_PLAYERS:
                    modalContent.innerText = "Waiting for players...\n Join Code: ";
                    const joinCode = document.createElement("button");
                    joinCode.classList.add("join-code");
                    joinCode.innerHTML = state.joinCode;
                    joinCode.setAttribute("title", "Copy code to clipboard");
                    copySvgPromise.then(svg => joinCode.append(svg))

                    joinCode.addEventListener("click", () => {
                        navigator.clipboard.writeText(state.joinCode).then(() => {
                            joinCode.setAttribute("copied", "true");
                            setTimeout(() => {
                                joinCode.setAttribute("copied", "false");
                            }, 1500);
                        }, () => {
                            console.log("Couldn't copy join code to clipboard");
                        })
                    })


                    modalContent.append(joinCode);
                    break;
                case GameStatus.PLAYER_DISCONNECTED:
                    modalContent.innerText = "Player has disconnected; waiting for them to reconnect";
            }
            modal.showModal();
        } else
            modal.close();
    })
}

function placePiece(index: number) {
    fetch(`${API_ROUTE_URI}/${gameId}/play-piece/${index}`, {
        method: "POST"
    }).then(res => {
        if(!res.ok)
            throw new Error();
    })
}

function requestRematch() {
    let action= "send";

    if(lastState.playerOneRematch && player.playerRole == "PLAYER_ONE" ||
        lastState.playerTwoRematch && player.playerRole == "PLAYER_TWO"
    ) {
        action = "withdraw";
    }

    fetch(`${API_ROUTE_URI}/${gameId}/rematch-request/${action}`, {
        method: "POST"
    }).then(res => {
        if(!res.ok)
            throw new Error();
    })
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
    WAITING_FOR_PLAYERS,
    PLAYER_DISCONNECTED
}

interface GameTile {
    pieceType: number,
    canPlace: boolean
}

interface GameState {
    gameTiles: GameTile[]
    isPlayerOneTurn: boolean
    gameStatus: GameStatus,
    playerOneRematch: boolean,
    playerTwoRematch: boolean,
    joinCode: string
}

interface PlayerData {
    playerRole: string
}