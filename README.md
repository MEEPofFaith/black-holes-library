# Mindustry Black Hole Renderer

This is a library mod used for the rendering of black holes.

## Why this mod?

While it would be really easy to just take my black hole renderer and copy it into other mods, it'll encounter the problem of multiple frame buffers starting on the same layer, thus causing the game to crash.

By using a singular library mod, all mods use my black hole renderer will send their data to the same renderer and thus there are no conflicts.

Note that mods that use this must depend on `MEEPofFaith/MindustryBlackHoleRenderer` and that this mod must be installed so that other mods can use it.

## How to use

1. Add the Jitpack repository to your repo
```groovy
repositories { maven { url 'https://jitpack.io' } }
```
2. Add this mod to your dependencies
```groovy
dependencies {
    compileOnly 'com.github.MEEPofFaith:MindustryBlackHoleRenderer:<version>'
}
```
See [releases](https://github.com/MEEPofFaith/MindustryBlackHoleRenderer/releases) for latest version.
3. Now that this mod has been added to your gradle. You just need to call `BlackHoleRenderer::addBlackHole` to draw black holes.

Additionally includes `SwirlEffect` to draw the particles that fall in.
