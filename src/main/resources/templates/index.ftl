<#-- @ftlvariable name="state" type="dev.aasmart.game.GameState" -->
<#import "_layout.ftl" as layout />

<@layout.main>
    <h2 id="state-title">
        Loading game...
    </h2>
    <div class="flex centered column" style="--gap: 1rem">
        <div class="gameBoard">
            <#list state.gameTiles as tile>
                <button class="gameTile" onclick="placePiece(${tile?counter - 1})"></button>
            </#list>
        </div>

        <div class="flex row centered game-buttons" style="--gap: 1rem">
            <button type="submit" class="basic-button" id="play-again" onclick="requestRematch()" disabled data-action="normal">
                Request Rematch
            </button>
            <button type="submit" class="basic-button" id="leave-game" onclick="exitGame()" disabled data-action="destructive">
                Quit Game
            </button>
        </div>
    </div>
    <script> <#include "./game.js"> </script>
</@layout.main>