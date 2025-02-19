# JalvaAddon

This is an addon for Meteor Client in Minecraft.

## Release 0.7 of JalvaAddon

### Biome Color Changer (WIP):
- Lets you change the water color, sky color, foliage color and grass color of the biomes in-game.
- Has a default color config and a biome specific color config.

### Chunk Trailer
- Lets you generate random chunk trails with elytra or save a manual replay with custom checkpoints.
- You can manage flight files in the `meteor-client/trail-replays` folder.
- The **Generate** mode lets you change the angle of deviation from the player's camera and distance to generate random checkpoints in the overworld.
- The **Save** mode lets you place custom checkpoints with a hotkey and is nether compatible.
- The **Load** mode lets you replay a file from the nearest checkpoint to the player, and also on reverse mode among other settings.

### Elytra Utils
- Auxiliary module for Chunk Trailer, but can also be used as a standalone one.
- Has some ElytraFly utils such as elytra recast, automatic use of rockets, elytra replace, etc.
- Also includes a speedometer.

### Map Boundaries
- Highlights the map region boundaries in-game. Useful for making map art machines/plots.
- You can change the color of the highlight.

### Block Replacer
- It places blocks from your inventory in a certain position. Useful when using feed tapes / conveyors.

## Contribute:  

You can contribute or make your own branch by following these steps:
- Clone this project with `git clone https://github.com/Jalvaviel/JalvaAddon.git` or download the zip of the source code.
- Open the project with your favourite IDE (I recommend **JetBrains IntelliJ**)
- Let gradle build the dependencies before making any changes.
- To build your .jar file, run the gradle `build` task. If you want to test it, run the gradle `runClient` task.
