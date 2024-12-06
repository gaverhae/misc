[Previous](./Chapter5.md) | [Next](./Chapter7.md)

<hr>

# Chapter 6 - Rendering Virtual Worlds

Sometimes, the game world is bigger than what is shown on the screen.

## Tile Maps

Often, a big world will be composed of repeating patterns.

### The Idea Behind Tile Maps

Tiles are small images designed to fit each other without breaking the pattern:
if we draw two compatible tiles side-by-side, it is not obvious where one ends
and the next one begins.

If our world is composed of tiles, instead of storing one huge image for the
world, we can store the world as a tile map: a rectangular image that contains
a single instance of each tile, and a table of integers, where each integer is
the index of a tile in the image file.

### Implementing a Tile Map

We use square tiles as they are a bit simpler; other shapes are possible, and
the principles are exactly the same.

The images for this chapter, a tileset and a few objects, can be found [here][0].

[0]: https://github.com/Apress/pro-android-web-game-apps/tree/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/code/v.05/img

Let's write those tiles on the screen. We'll assume we have the world
description, as a 2-dimensional array, in [a separate `world.js` file][1]
defining the `world` variable. We then design a `MapRenderer` class:

[1]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/code/v.01/js/world.js

```javascript
function MapRenderer(mapData, image, tileSize) {
  this._mapData = mapData;
  this._image = image;
  this._tileSize = tileSize;
  this._x = this._y = 0;
  this._tilesPerRow = image.width / tileSize;
}
_p = MapRenderer.prototype;
```

We assume square tiles here, hence a single parameter for their size, and the
rather easy computation of tiles per row. The `_x` and `_y` properties are
meant to represent the point at which we draw the map; i.e. if the user
"scrolls" the map we'll update them.

```javascript
_p.draw = function(ctx) {
  for (var cellY = 0; cellY < this._mapData.length; cellY++) {
    for (var cellX = 0; cellX < this._mapData[cellY].length; cellX++) {
      var tileId = this._mapData[cellY][cellX];
      this._drawTileAt(ctx, tileId, cellX, cellY);
    }
  }
};
_p._drawTileAt = function(ctx, tileId, cellX, cellY) {
  var srcX = (tileId % this._tilesPerRow) * this._tileSize;
  var srcY = Math.floor(tileId / this._tilesPerRow) * this._tileSize;
  var size = this._tileSize;
  var destX = this._x + cellX * size;
  var destY = this._y + cellY * size;
  ctx.drawImage(this._image, srcX, srcY, size, size, destX, destY, size, size);
};
_p.move = function(deltaX, deltaY) {
  this._x += deltaX;
  this._y += deltaY;
};
```

See [book code][2] for a full usage example.

[2]: https://github.com/Apress/pro-android-web-game-apps/tree/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/code/v.01

There are two main issues with that code:

- We redraw the entire map on every frame, regardless of whether we can see all
  of it.
- We redraw the enitre map on every frame, regardless of whether it has
  changed.

We will see various ways to address both of these issues over the next few
sections, but first, let's put in place some measurement so we can see the
impact of our efforts.

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

When drawing tiles, we can easily compute a bouding box for the screen. Objects
can have any position, and possibly move. We don't want to draw the ones that
are outside the screen if we can avoid it.

We basically have two options:

1. Look at all the objects, for each render. This is viable for [some
   games][6], but not most of them.
2. Try to do something smart. Because of its wide range of applications, this
   problem has been heavily researched (e.g. [R-Tree][7]).

[6]: https://tiny-island-survival.fandom.com/wiki/Main_Island
[7]: https://en.wikipedia.org/wiki/R-tree

A simple option is to expand again on our offscreen buffer idea: we divide the
world in a big grid, and organize objects by cells in that grid. It id then
easy to check which cells of the grid intersect with the viewport, and loop
only on those objects. In this model, it is possible for an object to be in
multiple cells, because a cell would "contain" all the objects that intersect
with it.

> Note that we are optimizing _the render loop_ here. This way of organizing
> the game objects may not necessarily make the "state update loop" faster.

## Isometric View

<hr>

[Previous](./Chapter5.md) | [Next](./Chapter7.md)
