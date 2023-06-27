const ip = "127.0.0.1:8080"
const SOCKET_ROUTE_URI = `ws://${ip}/api/game`
const API_ROUTE_URI = `http://${ip}/api/game`

let client: WebSocket;
let player: PlayerData;
let lastState: GameState;
let gameId: string;
let isPlayerOne: boolean;

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
              isPlayerOne = player.playerRole == "PLAYER_ONE"
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
                if(isPlayerOne)
                    title = "It's your turn!";
                else
                    title = "It's Player 1's turn!"
            else if(!state.isPlayerOneTurn)
                if(!isPlayerOne)
                    title = "It's your turn!";
                else
                    title = "It's Player 2's turn!"
            break;
        case GameStatus.PLAYER_ONE_WON:
            if(isPlayerOne)
                title = "You won!"
            else
                title = "Player One wins!"
            break;
        case GameStatus.PLAYER_TWO_WON:
            if(!isPlayerOne)
                title = "You won!"
            else
                title = "Player Two wins!"
            break;
        case GameStatus.PLAYER_ONE_FORFEIT:
            if(!isPlayerOne)
                title = "You won by forfeit!"
            else
                title = "Player Two wins by forfeit!"
            break;
        case GameStatus.PLAYER_TWO_FORFEIT:
            if(isPlayerOne)
                title = "You won by forfeit!"
            else
                title = "Player Two wins by forfeit!"
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

function showDialogModal(
    modalContentNodes: Node[],
    buttons: HTMLButtonElement[],
    displayLoadingBar: boolean
) {
    const modal: HTMLDialogElement = document.getElementById("popup") as HTMLDialogElement;

    const modalContent = modal.getElementsByTagName("p")[0];
    const modalButtons = document.getElementById("popup-buttons");

    while (modalButtons.firstChild)
        modalButtons.removeChild(modalButtons.lastChild);
    while (modalContent.firstChild)
        modalContent.removeChild(modalContent.lastChild);

    modal.toggleAttribute("data-is-loading", displayLoadingBar);
    modalContent.append(...modalContentNodes);
    modalButtons.append(...buttons);

    if(!modal.open)
        modal.showModal();
}

function closeDialogModal() {
    const modal: HTMLDialogElement = document.getElementById("popup") as HTMLDialogElement;
    modal.close()
}

function handleGameState(tiles: Element[], state: GameState) {
    closeDialogModal();

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
            state.isPlayerOneTurn && isPlayerOne ||
            !state.isPlayerOneTurn && !isPlayerOne;

        tile.toggleAttribute("disabled", (!tileState.canPlace || !canPlay));
        tile.classList.toggle("canPlace", tileState.canPlace && canPlay);
    })

    document.getElementById("state-title").innerText = getTitleString(state);

    handleRematchDisplay(state)

    const forfeitButton = document.getElementById("leave-game");
    forfeitButton.toggleAttribute(
        "disabled",
        state.gameStatus == GameStatus.WAITING_FOR_PLAYERS || state.gameStatus == GameStatus.PLAYER_DISCONNECTED
    );
    if(state.gameStatus == GameStatus.ACTIVE || state.gameStatus == GameStatus.PLAYER_DISCONNECTED)
        forfeitButton.innerText = "Forfeit";
    else
        forfeitButton.innerText = "Exit Game";

    if(state.gameStatus == GameStatus.WAITING_FOR_PLAYERS) {
        const joinCode = document.createElement("button");
        joinCode.classList.add("join-code");
        joinCode.innerHTML = state.joinCode;
        joinCode.setAttribute("type", "button");
        if(window.isSecureContext && navigator.clipboard) {
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

            joinCode.setAttribute("data-can-copy", "true");
        } else
            joinCode.setAttribute("data-can-copy", "false");

        const exitGameButton = document.createElement("button");
        exitGameButton.addEventListener("click", () => exitGame());
        exitGameButton.classList.add("basic-button");
        exitGameButton.setAttribute("data-action", "destructive");
        exitGameButton.innerText = "Exit Game";
        exitGameButton.setAttribute("type", "button");

        showDialogModal(
            [
                document.createTextNode("Waiting for players...\n Join Code: "),
                joinCode
            ], [
                exitGameButton
            ],
            true
        );
    } else if(state.gameStatus == GameStatus.PLAYER_DISCONNECTED) {
        showDialogModal(
            [
                document.createTextNode("Player has disconnected; waiting for them to reconnect"),
            ],
            [],
            true
        );
    }
}

function handleRematchDisplay(state: GameState) {
    const replayButton = document.getElementById("play-again");

    if(
        ((state.playerOneRematch && isPlayerOne) ||
        (state.playerTwoRematch && !isPlayerOne)) &&
        !state.rematchDenied
    ) {
        const cancelRematchRequestButton = document.createElement("button");
        cancelRematchRequestButton.classList.add("basic-button");
        cancelRematchRequestButton.setAttribute("data-action", "destructive");
        cancelRematchRequestButton.innerText = "Withdraw Rematch Request";
        cancelRematchRequestButton.addEventListener("click", () => requestRematch())
        cancelRematchRequestButton.setAttribute("type", "button");

        showDialogModal(
            [
                document.createTextNode("Your rematch request was sent")
            ],
            [
                cancelRematchRequestButton
            ],
            true
        )
    } else if((state.playerOneRematch || state.playerTwoRematch) && !state.rematchDenied) {
        const confirmButton = document.createElement("button");
        confirmButton.classList.add("basic-button");
        confirmButton.setAttribute("data-action", "normal");
        confirmButton.innerText = "Accept Rematch";
        confirmButton.addEventListener("click", () => requestRematch());
        confirmButton.setAttribute("type", "button");

        const cancelButton = document.createElement("button");
        cancelButton.classList.add("basic-button");
        cancelButton.setAttribute("data-action", "destructive");
        cancelButton.innerText = "Reject";
        cancelButton.addEventListener("click", () => {
            fetch(`${API_ROUTE_URI}/${gameId}/rematch-request/reject`, {
                method: "POST"
            }).then(res => {
                if(!res.ok)
                    throw new Error();
            })
        })
        cancelButton.setAttribute("type", "button");

        showDialogModal(
            [
                document.createTextNode("Your opponent has requested a rematch")
            ],
            [
                confirmButton,
                cancelButton
            ],
            false
        )
    } else if(
        ((state.playerOneRematch && isPlayerOne) || (state.playerTwoRematch && !isPlayerOne)) &&
        state.rematchDenied
    ) {
        const confirmButton = document.createElement("button");
        confirmButton.classList.add("basic-button");
        confirmButton.setAttribute("data-action", "normal");
        confirmButton.innerText = "Okay";
        confirmButton.addEventListener("click", () => requestRematch());
        confirmButton.setAttribute("type", "submit");

        showDialogModal(
            [
                document.createTextNode("Your opponent declined a rematch")
            ],
            [
                confirmButton
            ],
            false
        )
    }

    replayButton.innerText = "Request Rematch";
    replayButton.toggleAttribute(
        "disabled",
        (
            state.gameStatus == GameStatus.ACTIVE ||
            state.gameStatus == GameStatus.WAITING_FOR_PLAYERS ||
            !state.playerOneConnected ||
            !state.playerTwoConnected ||
            state.rematchDenied
        )
    );
    replayButton.setAttribute("data-action", "normal");
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

    if((lastState.playerOneRematch && isPlayerOne) ||
        (lastState.playerTwoRematch && !isPlayerOne)
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

function exitGame() {
    if(lastState.gameStatus != GameStatus.ACTIVE) {
        window.location.href = `http://${ip}`;
        return;
    }

    const confirmButton = document.createElement("button");
    confirmButton.onclick = exitGame;
    confirmButton.classList.add("basic-button");
    confirmButton.setAttribute("data-action", "destructive");
    confirmButton.innerText = "Confirm";
    confirmButton.addEventListener("click", () => {
        fetch(`${API_ROUTE_URI}/${gameId}/forfeit`, {
            method: "POST"
        }).then(res => {
            if(!res.ok)
                throw new Error();
        })
    });

    const cancelButton = document.createElement("button");
    cancelButton.onclick = exitGame;
    cancelButton.classList.add("basic-button");
    cancelButton.setAttribute("data-action", "normal");
    cancelButton.innerText = "Cancel";
    cancelButton.addEventListener("click", () => closeDialogModal())

    showDialogModal(
        [
            document.createTextNode("Are you sure you want to forfeit the game?")
        ],
        [
            confirmButton,
            cancelButton
        ],
        false
    )
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
    PLAYER_DISCONNECTED,
    PLAYER_ONE_FORFEIT,
    PLAYER_TWO_FORFEIT,
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
    joinCode: string,
    playerOneConnected: boolean,
    playerTwoConnected: boolean,
    rematchDenied: boolean
}

interface PlayerData {
    playerRole: string
}