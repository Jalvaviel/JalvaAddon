# JalvaAddon

This is an addon for Meteor Client in Minecraft.

## Release 0.6 of JalvaAddon

### Biome Color Changer:
- Lets you change the water color, sky color, foliage color and grass color of the biomes in-game.
- Has a default color config and a biome specific color config.

### Chunk Trailer
- Lets you generate chunk trails with elytra.
- Make replays of the flight trajectories and repeat them later.
- Change the angle of deviation from the player's camera to generate random waypoints.
- Change the waypoint distance generation.
- See the flight stats in-game after finishing the chunk trail.
- Let's you see the waypoints in-game with an overlay.

### Elytra Boost +
- Meteors boost sucks ass, so I ""borrowed"" Thunderhack's implementation for 2b2t.
- Boost by pressing the spacebar or let the module boost for you.
- Set a certain height and upwards speed as an autopilot.
- Set a speed limit.
- Auto recast and auto replace for elytra.

### Fast Breaker
- A little bit scuffed, but you can instamine blocks with a packet exploit.
- Packet limiter and a mining threshold which can be modified.

### Map Boundaries
- Highlights the map region boundaries in-game. I used it to delimit plots on MAI5.
- Change the color of the highlight.

## Contribute:  

You can contribute or make your own branch by following these steps:
- Clone this project with `git clone https://github.com/Jalvaviel/JalvaAddon.git` or download the zip of the source code.
- Open the project with your favourite IDE (I recommend **JetBrains IntelliJ**)
- Let gradle build the dependencies before making any changes.
- To build your .jar file, run the gradle `build` task. If you want to test it, run the gradle `runClient` task.
