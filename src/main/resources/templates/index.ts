window.addEventListener("load", () => {
    const joinForm = <HTMLFormElement>document.getElementById("join-game");
    const joinCodeInput = <HTMLInputElement>document.getElementById("join-code");

    const errorElementId = joinCodeInput.getAttribute("aria-errormessage")
    const errorMessage: HTMLElement = document.getElementById(errorElementId);

    joinForm.addEventListener("submit", (e) => {
        e.preventDefault();

        const formData = new FormData(joinForm);
        const joinCode = formData.get("join-code");

        fetch(`/api/game/join?join-code=${joinCode}`, {
            method: "POST",
            headers: {
                Accept: "application/json"
            }
        }).then(res => {
            if(!res.ok) {
                return res.text().then(text => {
                    throw new Error(text);
                });
            }

            return res.json();
        }).then(data => {
            const game = data as Game;
            joinCodeInput.toggleAttribute("aria-invalid", false);

            window.location.href = `/game/${game.id}`
        }).catch(err => {
            joinCodeInput.toggleAttribute("aria-invalid", true);
            errorMessage.innerText = err.message;
        })

        return false;
    });

    const joinCodeInteract = () => {
        joinCodeInput.toggleAttribute("aria-invalid", false);
        errorMessage.innerText = "";
    }

    joinCodeInput.addEventListener("change", joinCodeInteract);
    joinCodeInput.addEventListener("keyup", joinCodeInteract);
})

type Game = {
    id: number
}