<#import "_layout.ftl" as layout />

<@layout.main>
    <div class="game-form">
        <form action="/api/game" method="post">
            <h3>Create New Game</h3>
            <button class="basic-button">
                Create Game
            </button>
        </form>

        <form id="join-game" action="/api/game/join" method="get">
            <h3>Join Existing Game</h3>
            <label for="game-id">
                Join Code:
                <input type="text" name="join-code" id="join-code" required>
                <p></p>
            </label>
            <button type="submit" class="basic-button">
                Join Game
            </button>
        </form>
    </div>

    <script> <#include "./index.js"> </script>
</@layout.main>