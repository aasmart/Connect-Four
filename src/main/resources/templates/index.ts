window.addEventListener("load", () => {
    const joinForm = <HTMLFormElement>document.getElementById("join-game");
    const joinCodeInput = <HTMLInputElement>document.getElementById("join-code");

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
                console.log(res);
                throw new Error();
            }

            return res.json();
        }).then(data => {
            const game = data as Game;
            joinCodeInput.toggleAttribute("data-invalid", false);

            window.location.href = `/game/${game.id}`
        }).catch(err => {
            joinCodeInput.toggleAttribute("data-invalid", true);
        })

        return false;
    });
})

type Game = {
    id: number
}