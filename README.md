# Connect-Four
A multiplayer Connect Four web-game backend implemented using the Kotlin language and [Ktor](https://ktor.io/). This is my first major backend project as a goal to better understand the development process and how APIs, Websockets, Authentication, and more work.

To try it out, just clone the repo and run it on your local machine. A jar likely wont be released until I consider this project finished.

## Features
* Standard Connect Four gameplay
* Player disconnection timeout
* Games are stored in a database so they can be accessed even when the server is shutdown

## Planned Features
* Game spectating
* Integrate with a remote database (not a local one)
* Game customization

I do not have a fixed timeline for this application, so I'll work on these features when I feel like it.

## Frontend
The frontend for this application is being developed separately and can be found [here](https://github.com/aasmart/connect-four-frontend)
