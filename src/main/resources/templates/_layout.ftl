<#macro main>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport"
              content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
        <meta http-equiv="X-UA-Compatible" content="ie=edge">
        <title>Connect Four</title>

        <style> <#include "./styles.css"> </style>
    </head>
        <body>
            <dialog id="popup" data-is-loading>
                <div class="loading-bar">
                    <span style="--animation-delay: 0ms; --color: red"></span>
                    <span style="--animation-delay: 250ms; --color: gold"></span>
                    <span style="--animation-delay: 500ms; --color: red"></span>
                </div>
                <p></p>
                <div id="popup-buttons" class="flex row centered" style="--gap: 1rem"></div>
            </dialog>

            <main>
                <div>
                    <h1>
                        Connect Four
                    </h1>
                    <hr />
                </div>
                <#nested>
            </main>
        </body>
    </html>
</#macro>