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

Images can be loaded either from a file or from a [data URL]. In both cases,
the process is: create an `Image` object, then assign its `src` property. The
browser will then load the image asynchronously, and call the provided `onload`
callback once the image is successfully loaded, or the `onerror` callback if
something went wrong.

[data URL]: https://developer.mozilla.org/en-US/docs/Web/URI/Schemes/data

Given that image loading is asynchronous and a game will generally need many
images, we may want to create an "image loading" process that can load multiple
images while reporting on its progress. The [book code][ImageManager] contains
an `ImageManager` class that does just that.

[ImageManager]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/js/ImageManager.js

[Example usage][ImageManager html]:

[ImageManager html]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/01.basic_image_manager.html

```javascript
var imageManager = new ImageManager();
imageManager.load({
  "arch-left": "img/arch-left.png",
  "arch-right": "img/arch-right.png",
  "knight": "img/knight.png"
}, onDone, onProgress);
```

where `onDone()` is called once when all the images have loaded, and
`onProgress(loaded, total, key, path, success)` is called after each image has
finished loading.

> The rest of the section explains the ImageManager code line-by-line, which is
> not very interesting as the code is fairly straightforward. The one item of
> note is that the author chose for each call to `load` to create a separate
> counter, so calling `load` multiple times is not the same as calling it once
> with the union of the images. The author seems to think that's a good thing.

Data URLs load faster and don't fail on network calls, but they do make the
JavaScript itself larger and thus slower to load. They are nice for very small
or fallback/placeholder images.

One easy way to create a data URL is to use the [`toDataURL` method on
canvas][1].

[1]: https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement/toDataURL

You'll have to make a call on which images to load when (e.g. level-specific
images may wait for the player to reach that level), and how to package them
(data URLs vs. files, one or multiple images per file).

> The book does give _some_ more specific guidance on file sizes, but I reckon
> most of it is moot 13 years and a major HTTP version later.

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

Along with a sprite sheet, an artist should provide, for each sprite, its size
and anchor point. The anchor point is the position of the sprite in the game
world. For example, if a character bows, their feet should stay in place even
though the image will be smaller in height; if they raise their armes, their
feet should still not move even though the sprite will be bigger in height.

We therefore need 6 numbers for each sprite: its top-left corner within the
sprite sheet, its dimensions, and the position of the anchor point (relative to
the top-left corner of the sprite).

Given those, we can draw the sprite at `x, y` in the game world with:

```javascript
ctx.drawImage(
  sprite.sheet,
  // "source" rectangle in the sprite sheet
  sprite.frame_x, sprite.frame_y, sprite.frame_width, sprite.frame_height,
  // "destination" rectangle in the canvas
  x - sprite.anchor_x, y - sprite.anchor_y, sprite.frame_width, sprite.frame_height);
```

The book creates [a class for that][4] and shows [example usage][5].

[4]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/js/SpriteSheet.js
[5]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/04.sprite_sheet.html

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

The browser presents a single-threaded environment. This means that only one
piece of JavaScript code can run at any time, and it runs without interruption
until it reaches its own, natural end. While it runs, the entire browser (well,
that one page) is frozen.

Therefore, JavaScript code should terminate quickly.

The browser model lets the programmer register callbacks for various events
directly in JavaScript (e.g. `onload`, `onclick`), and JavaScript code can
register its own callbacks based on future events (`onload` on `Image`), or
just time passing (see [the `setTimeout` method][setTimeout]).

[setTimeout]: https://developer.mozilla.org/en-US/docs/Web/API/Window/setTimeout

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
