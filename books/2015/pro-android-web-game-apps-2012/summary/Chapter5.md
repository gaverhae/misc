[Previous](./Chapter4.md) | [Next](./Chapter6.md)

<hr>

# Chapter 5 - Event Handling and User Input

## Browser Events

Browser events on desktop and mobile are different. At the time of writing,
mobile browser events on Android basically amount to a single-button mouse, so
this chapter focuses on that.

> Since then, a lot has happened in the browser event space, the most notable
> perhaps being the standardized [Sensors APIs][0]. I don't really care about
> those at this time, though; the "one-button mouse" model is fine for my
> purposes.

[0]: https://developer.mozilla.org/en-US/docs/Web/API/Sensor_APIs

When an event happens, event listeners are called with an event object as their
first parameter to provide more details than just "the event happened".

The `onEVENT` DOM attribute can be used to define a snippet of JavaScript code
as a listener for a given `EVENT`, for example:

```html
<body onload="init()">
...
</body>
```

will execute `init()` when the `load` event happens.

> The book does not mention that you can use the special variable name `event`
> in such snippets to get access to the actual event, if needed, as in:
> ```html
> <body onload="init(event)">...</body>
> ```

We can also register events through JavaScript:

```javascript
var el = document.getElementById('canvas');
var my_event_listener = (ev) => { console.log(ev); };
el.addEventListener('touchstart', my_event_listener);
```

Outside of code organization (mixing JS and HTML is sometimes frowned upon),
the main advantage of this approach is that we can register multiple listeners
for the same event.

After all of the listeners for an event on an element have ran, the same event
is sent to the parent node, which also fires all of its listeners for that
event, and so on. There are two ways to change that:

- `e.stopPropagation()` will stop the event from bubbling up to the parents of
  the current element.
- `e.preventDefault()` will prevent the default event handlers from running on
  the current element.

We can detect touch devices by the fact that they have a touch event emitter:

```javascript
'ontouchstart' in document.documentElement
```

> In the correspondence between DOM and HTML, `document.documentElement` is the
> `<html>` node.

## Our Own Event System

> I personally think core.async channels are a much superior option.

Events can also be used to decouple components within our own code (e.g.
`coin_collected`). The browser [has some support for that][1], which you should
use, but in order to explain the concepts we'll build our own here.

[1]: https://developer.mozilla.org/en-US/docs/Web/Events/Creating_and_triggering_events

Say we want to write:

```javascript
levelManager.on('loaded', (e) => {
  /* ... */
});
```

to let other parts of our application know that the level has been loaded. We
will build an `EventEmitter` class that `LevelManager` can inherit from to
provide that functionality.

```javascript
function EventEmitter() {
  this._listeners = {};
}
_p = EventEmitter.prototype;

_p.addListener = p._on = (type, listener) => {
  if (typeof listener !== 'function') throw "listener must be a function";
  if (!this.listeners[type]) {
    this._listeners[type] = [];
  }
  this._listeners[type].push(listener);
};
_p.removeListener = (type, listener) => {
  /* ... */
};
_p.removeAllListeners = (type) => {
  if (type) {
    this._listeners[type] = [];
  } else {
    this._listeners = {};
  }
};
_p.emit = (type, event) => {
  if ( (! this._listeners[type]) || (! this._listeners[type].length)) {
    return;
  }
  for (var i = 0; i < this._listeners[type].length; i++) {
    this._listeners[type][i].apply(this, event);
  }
};
```

> This design assumes that anything that wants to emit events needs to be
> implemented as a subclass of EventEmitter. I'm not sure I like that.
>
> I'm also not too fond of the `apply` call in `emit`. It implies that the
> callback executes in the context of the emitter of the event, wheres it seems
> to me it would make a lot more sense if it happened in the context of the
> listener.

In Chapter 4, we designed the `ImageLoader` class with a callback API:

```javascript
imageManager.load({
  house: "img/house.png",
  knight: "img/knight.png"
}, () => {
  /* runs when loading is done */
});
```

We could instead have gone for an event API:

```javascript
imageManager.on('done', (e) => { /* ... */ });
imageManager.addImage('house', 'img/house.png');
imageManager.loadImages();
```

It's mostly a matter of style, but the event approach allows for multiple
callbacks on the same event.

## "One-Button Mouse" Events

We want to build a single set of synthetic events to cover the differences
between touch and click devices:

- `down` covers `mousedown` and `touchstart`. The main difference here is that
  `touchstart` can have multiple locations in the case of a multi-touch screen.
  In that case, we'll ignore all but the first listed location.
- `up` covers `mouseup` and `touchend`, with the main difference being that
  `touchend` does not provide coordinates, so we have to track the last move.
- `move` covers `mousemove` and `touchmove`, but `mousemove` only when the
  button is pressed (i.e. we ignore hovering).

We also want `move` events to include the delta, as it is often useful.
Finally, we want to filter out unintentional movement to be able to detect taps
as opposed to short drags.

We will write two classes, one for mouse and one for touch screen, that
implement the same interface, so the rest of our code does not need to know
which one we have. We start with the base class:

```javascript
function InputHandlerBase(element) {
  EventEmitter.call(this);
  this._element = element;
  this._lastMoveCoordinates = null;
  this._moving = false;
  this._moveThreshold = 10;
}
extend(InputHandlerBase, EventEmitter);
_p = InputHandlerBase.prototype;

_p._getInputCoordinates = (e) => {
  var element = this._element;
  var coords = e.targetTouches ? e.targetTouches[0] : e;
  return {
    x: coords.pageX,
    y: coords.pageY
  };
};
_p._onDownDomEvent = (e) => {
  var coords = this._lastMoveCoordinates = this._getInputCoordinates(e);
  this.emit('down', {x: coords.x, y: coords.y, domEvent: e});
  e.stopDefault();
  e.stopPropagation();
};
_p._onMoveDomEvent = (e) => {
  var coords = this._getInputCoordinates(e);
  var dx = coords.x - this._lastMoveCoordinates.x;
  var dy = coords.y - this._lastMoveCoordinates.y;
  if ( (!this._moving) && (Math.sqrt(dx * dx + dy * dy) > this._moveThreshold)) {
    this._moving = true;
  }
  if (this._moving) {
    this.emit('move', {x: coords.x, y: coords.y, dx, dy, domEvent: e});
    this._lastMoveCoordinates = coords;
  }
  e.stopDefault();
  e.stopPropagation();
};
_p._onUpDomEvent = (e) => {
  var coords = this._getInputCoordinates(e);
  this.emit('up', {x: coords.x, y: coords.y, moved: this._moving, domEvent: e});
  this._moving = false;
  e.stopDefault();
  e.stopPropagation();
};
```

The mouse handler needs to ignore hovering, and deal with the cursor moving out
of the app surface:

```javascript
function MouseInputHandler(el) {
  InputHandlerBase.call(this, el);
  this._mouseDown = false;
  this._attachDomListeners();
}
extend(MouseInputHandler, InputHandlerBase);
_p = MouseInputHandler.prototype;
_p._attachDomListeners = () => {
  var el = this._element;
  el.addEventListener('mousedown', this._onDownDomEvent.bind(this));
  el.addEventListener('mouseup', this._onUpDomEvent.bind(this));
  el.addEventListener('mousemove', this._onMoveDomEvent.bind(this));
  // in case user releases button outside of canvas
  el.addEventListener('mouseout', this._onMouseOut.bind(this));
};
_p._onDownDomEvent = (e) => {
  this._mouseDown = true;
  InputHandlerBase.prototype._onDownDomEvent.call(this, e);
};
_p._onUpDomEvent = (e) => {
  this._mouseDown = false;
  InputHandlerBase.prototype._onUpDomEvent.call(this, e);
};
_p._onMoveDomEvent = (e) => {
  if (this._mouseDown) {
    InputHandlerBase.prototype._onMoveDomEvent.call(this, e);
  }
}
_p._onMouseOut = (e) => {
  this._mouseDown = false;
};
```

The touch screen handler needs to track the finger position so we can provide
it on `up` events:

```javascript
function TouchInputHandler(element) {
  this._lastInteractionCoordinates = null;
  InputHandlerBase.call(this, element);
  this._attachDomListeners();
}
extend(TouchInputHandler, InputHandlerBase);
_p = TouchInputHandler.prototype;

_p._attachDomListeners = () => {
  var el = this._element;
  el.addEventListener('touchstart', this._onDownDomEvent.bind(this));
  el.addEventListener('touchend', this._onUpDomEvent.bind(this));
  el.addEventListener('touchmove', this._onMoveDomEvent.bind(this));
};
_p._onDownDomEvent = (e) => {
  this._lastInteractionCoordinates = this._getInputCoordinates(e);
  InputHandlerBase.prototype._onDownDomEvent.call(this, e);
};
_p._onUpDomEvent = (e) => {
  this.emit('up', {
    x: this._lastInteractionCoordinates.x,
    y: this._lastInteractionCoordinates.y,
    moved: this._moving,
    domEvent: e
  });
  this.lastInteractionCoordinates = null;
  this._moving = false;
  e.stopPropagation();
  e.preventDefault();
};
_p._onMoveDomEvent = (e) => {
  this._lastInteractionCoordinates = this._getInputCoordinates(e);
  InputHandlerBase.prototype._onMoveDomEvent.call(this, e);
};
```

With all of that, we still need an entrypoint:

```javascript
var InputHandler = 'ontouchstart' in document.documentElement ? TouchInputHandler : MouseInputHandler;
```

The rest of our code can simply run:

```javascript
var input = new InputHandler();
input.on('up', (e) => { /* ... */ });
input.on('down', (e) => { /* ... */ });
input.on('move', (e) => { /* ... */ });
```

and blissfully ignore what kind of input device we have, as long as our game is
defined in terms of only those three interactions.

> I am not impressed by the expressive power of inheritance here. I'm still a
> sucker for good old composition.
>
> Here's what it could look like:
>
> ```javascript
> const makeInput = (el) => {
>   const emitter = new EventEmitter();
>   let last_pos = null;
>   let moving = false;
>   let mouse_down = false;
>   const threshold = 10;
>   const event_to_position = (domEvent) => {
>     let e = domEvent.targetTouches ? domEvent.targetTouches[0] : domEvent;
>     return {x: e.pageX || last_pos.x, y: e.pageY || last_pos.y};
>   };
>   const on_down = (domEvent) => {
>     mouse_down = true;
>     last_pos = event_to_position(domEvent);
>     emitter.emit('down', {x: last_pos.x, y: last_pos.y, domEvent});
>   };
>   const on_move = (domEvent) => {
>     if (!mouse_down) return;
>     let c = event_to_position(domEvent);
>     let dx = c.x - last_pos.x;
>     let dy = c.y - last_pos.y;
>     if (Math.sqrt(dx * dx + dy * dy) > threshold) {
>       moving = true;
>     }
>     if (moving) {
>       emitter.emit('move', {x: c.x, y: c.y, dx, dy, domEvent});
>       last_pos = c;
>     }
>   };
>   const on_up = (domEvent) => {
>     mouse_down = false;
>     let c = event_to_position(domEvent);
>     emitter.emit('up', {x: c.x, y: c.y, moved: moving, domEvent});
>     moving = false;
>   };
>   const attach = (t, f) => {
>     el.addEventListener(t, (e) => {
>       f(e);
>       e.stopPropagation();
>       e.preventDefault();
>     });
>   };
>   if ('ontouchstart' in document.documentElement) {
>     attach('touchstart', on_down);
>     attach('touchmove', on_move);
>     attach('touchend', on_up);
>   } else {
>     attach('mousedown', on_down);
>     attach('mousemove', on_move);
>     attach('mouseup', on_up);
>     attach('mouseout', () => { mouse_down = false; });
>   }
>   return {
>     on: (t, f) => emitter.addListener(t, f),
>     off: (t, f) => emitter.removeListener(t, f),
>     removeAllListeners: (t) => emitter.removeAllListeners(t)
>   };
> }
> ```
>
> Usage would be roughly the same:
>
> ```javascript
> var input = makeInput();
> input.on('down', (e) => { /* ... */ };
> ```
>
> I believe this is much easier to follow, and it's also a little bit shorter.
> As a bonus, we're not dealing with `this` at all. Lexical scope FTW!
>
> See [my_input.html] for a complete example.

[my_input.html]: http://127.0.0.1:8080/ch5/my_input.html

## Advanced Input

A one-button-mouse model may seem limiting, but we can easily construct more
advanced controls on top of it. For example, drag-and-drop:

1. Pick: if the `down` event happens on a draggable object, start a drag.
2. Move: move the object along with `move` events.
3. Release: on `up`, release the object.

See [book code][2] for a full example.

[2]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/9781430238195_sourcecode_chp05/code/04.drag_and_drop.html

This assumes we have nice rectangles, but sometimes our shapes are more
complicated. After the `down` event, we have the coordinates of the selected
pixel. We can ask the canvas for the color of that pixel with:

```javascript
ctx.getImageData(x, y, 1, 1).data
```

which returns a 4-element array of RGBa. We can repaint a virtual canvas with a
mask of each of our sprites, assigning a specific, single color to each mask,
and then check the color of that pixel on that virtual canvas to find out which
element should be picked.

> In this context, a "virtual" canvas is one that is not mounted into the DOM. We
> can create such a canvas with:
>
> ```javascript
> var virtual_canvas = document.createElement("canvas");
> ```
>
> This is in all respects a "real" canvas that just happens not to be drawn on
> the screen. We can set its size with `virtual_canvas.height = 5` and get its
> context with `virtual_canvas.getContext('2d')` etc.

How do we create those masks? When we ask a canvas to draw a new shape, it is
composed with the existing content of the canvas in a configurable way. The
default composition operation is known as `source-over`, and means that the
existing content is simply painted over existing content. Other composition
modes exist, though.

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

> As an untested sketch:
>
> ```javascript
> var shapes = [{image: "...", x: 14, y: 18}, /* ... */];
> var canvas = document.getElementById("visibleCanvas");
> var buffer = document.createElement("canvas");
> var masks = document.createElement("canvas");
>
> var width = masks.width = canvas.width;
> var height = masks.height = canvas.height;
>
> function color_to_index(color) {
>   /* ... */
> }
>
> function index_to_color(index) {
>   /* ... */
> }
>
> function get_shape_index(x, y) {
>   var m = masks.getContext('2d');
>   m.clearRect(0, 0, width, height);
>   shapes.forEach((shape, index) => {
>     draw_mask(shape.image, shape.x, shape.y, index_to_color(index), m);
>   });
>   return color_to_index(m.getImageData(x, y, 1, 1).data);
> }
>
> function draw_mask(source, x, y, color, target) {
>   var w = buffer.width = source.width;
>   var h = buffer.height = source.height;
>   var b = buffer.getContext('2d');
>   b.drawImage(source, 0, 0, w, h, 0, 0, w, h);
>   b.globalCompositeOperation = 'source-atop';
>   b.fillStyle = color;
>   b.fillRect(0, 0, w, h);
>   target.drawImage(buffer, x, y);
> }
> ```

See [book code][3] for a complete, working example of pixel-perfect drag and
drop.

[3]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/9781430238195_sourcecode_chp05/code/06.pixel_picking.html

## Simulating Joystick

Simulating a joystick is a common way to implement controls on a touch screen.
It's not that great on a desktop, however. See [this complete example][4].

[4]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/9781430238195_sourcecode_chp05/code/07.joystick.html

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
