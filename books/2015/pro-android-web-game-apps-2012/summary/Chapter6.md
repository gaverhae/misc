[Previous](./Chapter5.md) | [Next](./Chapter7.md)

<hr>

# Chapter 6 - Rendering Virtual Worlds

## Tile Maps

### The Idea Behind Tile Maps

Tiles are small images designed to fit each other without breaking the pattern:
if we draw two compatible tiles side-by-side, it is not obvious where one ends
and the next one begins.

If our world is composed of tiles, instead of storing one huge image for the
world, we can store the world as a tile map: a rectangular image that contains
a single instance of each tile, and a table of integers, where each integer is
the index of a tile in the image file.

### Implementing a Tile Map

### Measuring FPS

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
