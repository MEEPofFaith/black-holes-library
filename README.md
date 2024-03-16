# Mindustry Black Hole Renderer

This is a library mod used for the rendering of black holes.

## Why this mod?

While it would be really easy to just take my black hole renderer and copy it into other mods, it'll encounter the problem of multiple frame buffers starting on the same layer, thus causing the game to crash.

By using a singular library mod, all mods use my black hole renderer will send their data to the same renderer and thus there are no conflicts.

Note that mods that use this must depend on `MEEPofFaith/MindustryBlackHoleRenderer` and that this mod must be installed so that other mods can use it. **Don't forget to also add `black-hole-renderer` to your `dependncies` in `mod.json`**

## How to use (Java)

1. Add the Jitpack repository to your repo.
```groovy
repositories { maven { url 'https://jitpack.io' } }
```
2. Add this mod to your dependencies. See [releases](https://github.com/MEEPofFaith/MindustryBlackHoleRenderer/releases) for latest version.
```groovy
dependencies {
    compileOnly 'com.github.MEEPofFaith:MindustryBlackHoleRenderer:<version>'
}
```
3. Now that this mod has been added to your gradle. You just need to call `BlackHoleRenderer::addBlackHole` to draw black holes.

    - Additionally includes `SwirlEffect` to draw the particles that spin around and fall in.

## Drawer and Part

This mod also includes a `DrawBlackHole` drawer and `BlackHolePart`
 part so that you don't need to make your own, and they are also compatable with json.

### Sample code:
#### DrawBlackHole
```json
{
   "type": "DrawBlackHole",
   "size": 5, //Radius of black hole
   "edge": 25, //Extent of the lensing effect
   "warmup": false //True by default. Whether to scale based on building warmup or always max size
}
```
```java
new DrawBlackHole(<size>, <edge>) //The size and edge can be set with a handy constructor
```
#### BlackHolePart
Structured very similarly to other parts.
```json
{
   "type": "BlackHolePart",
   "x": 1,
   "y": 1,
   "moveX": 1,
   "moveY": 1,
   "size": 1, //Size when grow progress is 0. 0 by default
   "sizeTo": 12, //Size when grow progress is 1
   "edge": 4, //Edge when grow progress is 0. 0 by default
   "edgeTo": 32, //Edge when grow progress is 1.
   "progress": "warmup",
   "growProgrss": "warmup"
}
```
You probably know how to translate json to java so I'm not gonna bother with a java example.
