<#-- @ftlvariable name="tiles" type="kotlin.collections.List<dev.aasmart.game.Tile>" -->
<#import "_layout.ftl" as layout />

<@layout.main>
    <h2>
        [TITLE]
    </h2>
    <div class="gameBoard">
        <#list tiles as tile>
            <#if tile.canPlace == true>
                <button class="gameTile canPlace" onclick="placePiece(${tile?counter - 1})"></button>
            </#if>
            <#if tile.canPlace == false>
                <#if tile.fall == true>
                    <button class="gameTile fall ${tile.pieceType}"></button>
                </#if>
                <#if tile.canPlace == false>
                    <button class="gameTile ${tile.pieceType}"></button>
                </#if>
            </#if>

        </#list>
    </div>

    <button class="replay basic-button">
        Replay Game
    </button>
    <script> <#include "./game.js"> </script>
</@layout.main>