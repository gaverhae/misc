[Previous](./Chapter3.md) | [Next](./Chapter5.md)

<hr>

# Chapter 4 - Animation and Sprites

Lines and rectangles only get you so far. At some point, you'll want real
images in your game, and they will likely come in the form of PNG files that
include several "sprites", like this:

![knight](../public/img/spritesheet.png)

## Sprites

A raster image is called a _sprite_, and when multiple sprites are in the same
image, as in the example above, we call that image a _sprite sheet_.

Raster images are somewhat simpler than vector graphics, but they don't scale
very well in either appearance or performance.

### Loading Images

#### Loading Images from Files

#### Loading Multiple Images

#### Using a Data URL as an Image

#### Tips on Loading Images

### Drawing an Image

The `"2d"` canvas has [a `drawImage` method][2], which takes 9 arguments: an
image, a "source" rectangle from that image, and a target rectangle on the
canvas.

[2]: https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/drawImage

Image drawing performance varies based on images, browsers, and hardware. Test
for your own game. Observations based on 2012 data:

- Drawing one big image is faster than multiple small images covering the same area.
- Scaling is slow.
- Drawing from a sprite sheet is slower than drawing from a single image; it
  may be worth to recreate in-memory copies of individual images after loading
  a sprite sheet from the network. Note that even in 2012 this is about a 10%
  difference.
- Decimal pixels kill performance; images should be drawn aligned with pixels.
  It also looks bad. Round coordinates.

If performance is a concern, prototype your game on real hardware.

### Sprite Sheets

## Basics of Animation

### The Simplest Animation

### JavaScript Threading Model

### Timers

### Improving Animation

<hr>

[Previous](./Chapter3.md) | [Next](./Chapter5.md)
