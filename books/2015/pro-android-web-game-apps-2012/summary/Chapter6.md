[Previous](./Chapter5.md) | [Next](./Chapter7.md)

<hr>

# Chapter 6 - Rendering Virtual Worlds

## Tile Maps

### The Idea Behind Tile Maps

### Implementing a Tile Map

### Measuring FPS

We use [xStats][3].

[3]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/code/v.01/js/xstats.js

> The original link mentioned in the book,
> `https://github.com/bestiejs/xstats.js`,  does not seem to exist anymore.
> However, the book does mention that it is a derivative of [this one][4]
> (without providing a link), which seems to still be maintained and provides
> quite a bit more data than the version in the book.

[4]: https://github.com/mrdoob/stats.js

> I should also note that, testing the provided code (at this point) on my
> phone in 2024, I sit very comfortably at 60fps, so the added value of the
> following optimizations will not be very visible on this example. For
> reference, the author claims only 27fps on his.

## Optimizing Rendering Performance

### Draw Only What Is Required

### Offscreen Buffer

### Catching the Area Around the Viewport

## World Objects

### Coordinate Systems

### Implementing WorldObjectRenderer

### Rendering Order

### Optimizations

## Isometric View

<hr>

[Previous](./Chapter5.md) | [Next](./Chapter7.md)
