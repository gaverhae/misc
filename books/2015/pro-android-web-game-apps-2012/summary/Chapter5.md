[Previous](./Chapter4.md) | [Next](./Chapter6.md)

<hr>

# Chapter 5 - Event Handling and User Input

## Browser Events

### Desktop Browser vs. Android Browser Input

### Using Events to Catch User Input

#### Event Object

#### Registering for Events: DOM Attributes

#### Registering for Events: Event Listeners

### Getting More from Events

### Handling the Differences Between Touch and Mouse Interfaces

## Custom Events

## Custom Event Listeners and Emitters

### EventEmitter: The Base Class

### Events vs. Callbacks

## Custom Events

### Implementing InputHandlerBase

### Creating MouseInputHandler

### Creating TouchInputHandler

## Advanced Input

### Drag-and-Drop

### Pixel-Perfect Picking and Image Masks

### Composite Operations

When we ask a canvas to draw a new shape, it is composed with the existing
content of the canvas in a configurable way. The default composition operation
is known as `source-over`, and means that the existing content is simply
painted over. Other composition modes exist, though.

The one we're interested in for our purposes here is `source-atop`, which means
that new pixels are only written if the existing canvas already had a non-empty
value at that position. With that option, we can easily:

1. Create a new, virtual canvas with the same dimensions as the image we're
   creating a mask for.
2. Draw the image on the canvas normally.
3. Change the composition operation to `source-atop`.
4. Draw a single-color rectangle on the entire canvas.

And voilà, we have a mask of the image with the given color.

In practice, we would typically work with two virtual canvas in addition to the
real one. Let's call them `buffer` and `masks`. We would use `buffer` to create
the individual masks, as described above, clearing and resizing `buffer`
instead of creating a new one on step 1 (creating DOM elements, even without
mounting them, can be expensive), and then as step 5 we would copy `buffer` on
top of `masks` at the right position. If we process our images in the same
order as for the real canvas, the color of the pixel on `masks` will directly
give us the image the user clicked on.

Drawing a canvas on top of another one is a simple call to `drawImage`, which
happens to accept a canvas as its first argument.

As an untested sketch:

```javascript
var shapes = [{image: "...", x: 14, y: 18}, /* ... */];
var canvas = document.getElementById("visibleCanvas");
var buffer = document.createElement("canvas");
var masks = document.createElement("canvas");

var width = masks.width = canvas.width;
var height = masks.height = canvas.height;

function color_to_index(color) {
  /* ... */
}

function index_to_color(index) {
  /* ... */
}

function get_shape_index(x, y) {
  var m = masks.getContext('2d');
  m.clearRect(0, 0, width, height);
  shapes.forEach((shape, index) => {
    draw_mask(shape.image, shape.x, shape.y, index_to_color(index), m);
  });
  return color_to_index(m.getImageData(x, y, 1, 1).data);
}

function draw_mask(source, x, y, color, target) {
  var w = buffer.width = source.width;
  var h = buffer.height = source.height;
  var b = buffer.getContext('2d');
  b.drawImage(source, 0, 0, w, h, 0, 0, w, h);
  b.globalCompositeOperation = 'source-atop';
  b.fillStyle = color;
  b.fillRect(0, 0, w, h);
  target.drawImage(buffer, x, y);
}
```

See [book code][3] for a complete, working example of pixel-perfect drag and
drop.

[3]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/9781430238195_sourcecode_chp05/code/06.pixel_picking.html

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
