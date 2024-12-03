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

### Timers

### Improving Animation

> This section describes the [Animator] class, pretty much line by line. I'll
> only summarize the concepts here, rather than the implementation details of
> that particular choice.

[Animator]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/js/Animator.js

The callback given to `requestAnimationFrame` will be called with one argument:
the current time. It should use it to compute the state it is rendering, rather
than count the number of times it has been called or look only at the time
passed since last frame.

> Note that the argument to the `requestAnimationFrame` callback is actually
> the time at which the last frame finished rendering **in milliseconds since
> page load**. It is therefore not a good idea to compare it to `new Date()`
> (milliseconds since **January 1st, 1970**, likely some time before the
> opening of the current page) as the author does in his code snippets, which
> predate the final standardization of `requestAnimationFrame`.

Here the state over time covers both object positions and sprite selection
(e.g. a character moving would both change position and change the sprite used
on each frame to show leg movement).

<hr>

[Previous](./Chapter3.md) | [Next](./Chapter5.md)
