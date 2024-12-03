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

### JavaScript Threading Model

### Timers

### Improving Animation

<hr>

[Previous](./Chapter3.md) | [Next](./Chapter5.md)
