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

### Sprite Sheets

## Basics of Animation

### The Simplest Animation

Animation works by drawing frames over time. The browser is in charge of screen
refreshes, so in that context it amounts to asking the browser to call a
function right after each screen refresh. That function must render the next
state of the game on the canvas, which the browser will then render on the
screen.

The way we ask the browser to run a function in-between two screen refreshes is
by calling [the `requestAnimationFrame` method][6].

[6]: https://developer.mozilla.org/en-US/docs/Web/API/Window/requestAnimationFrame

Note that `requestAnimationFrame` only schedules a single call; for prolonged
animations, one must make sure to call it again from within the scheduled
function.

### JavaScript Threading Model

### Timers

### Improving Animation

<hr>

[Previous](./Chapter3.md) | [Next](./Chapter5.md)
