<#-- @ftlvariable name="state" type="dev.aasmart.game.GameState" -->
<#import "_layout.ftl" as layout />

<@layout.main>
    <h2 id="state-title">
        Loading game...
    </h2>
    <div class="gameBoard">
        <#list state.gameTiles as tile>
            <button class="gameTile" onclick="placePiece(${tile?counter - 1})"></button>
        </#list>
    </div>

    <button class="basic-button" id="play-again" disabled>
        Play Again
    </button>
    <script> <#include "./game.js"> </script>
</@layout.main>