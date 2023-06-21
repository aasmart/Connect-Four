<#-- @ftlvariable name="state" type="dev.aasmart.game.GameState" -->
<#import "_layout.ftl" as layout />

<@layout.main>
    <h2 id="state-title">
        Loading game...
    </h2>
    <div class="gameBoard">
        <#list state.gameTiles as tile>
            <#if tile.canPlace == true>
                <button class="gameTile canPlace" onclick="placePiece(${tile?counter - 1})"></button>
            </#if>
            <#if tile.canPlace == false>
                <button class="gameTile ${tile.pieceType}" onclick="placePiece(${tile?counter - 1})"></button>
            </#if>
        </#list>
    </div>

    <button class="replay basic-button">
        Replay Game
    </button>
    <script> <#include "./game.js"> </script>
</@layout.main>