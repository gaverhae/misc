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

### JavaScript Threading Model

The browser presents a single-threaded environment. This means that only one
piece of JavaScript code can run at any time, and it runs without interruption
until it reaches its own, natural end. While it runs, the entire browser (well,
that one page) is frozen.

Therefore, JavaScript code should terminate quickly.

The broser model lets the programmer register callbacks for various events
directly in JavaScript (e.g. `onload`, `onclick`), and JavaScript code can
register its own callbacks based on future events (`onload` on `Image`), or
just time passing (see [the `setTimeout` method][setTimeout]).

[setTimeout]: https://developer.mozilla.org/en-US/docs/Web/API/Window/setTimeout

### Improving Animation

<hr>

[Previous](./Chapter3.md) | [Next](./Chapter5.md)
