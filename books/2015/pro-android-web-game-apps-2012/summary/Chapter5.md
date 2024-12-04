[Previous](./Chapter4.md) | [Next](./Chapter6.md)

<hr>

# Chapter 5 - Event Handling and User Input

## Browser Events

### Desktop Browser vs. Android Browser Input

Browser events on desktop and mobile are different. At the time of writing,
mobile browser events on Android basically mount to a single-button mouse, so
this chapter focuses on that.

> Since then, a lot has happened in the browser event space, the most notable
> perhaps being the standardized [Sensors APIs][0]. I don't really care about
> those at this time, though; the "one-button mouse" model is fine for my
> purposes.

[0]: https://developer.mozilla.org/en-US/docs/Web/API/Sensor_APIs

### Using Events to Catch User Input

Events are used to decouple components.

#### Event Object

When an event happens, event listeners are called with an event object as their
first parameter to provide more details than just "the event happened".

#### Registering for Events: DOM Attributes

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

#### Registering for Events: Event Listeners

We can also register events through JavaScript:

```javascript
var el = document.getElementById("canvas");
var my_event_listener = (ev) => { console.log(ev); };
el.addEventListener("touchstart", my_event_listener);
```

Outside of code organization (mixing JS and HTML is sometimes frowned upon),
the main advantage of this approach is that we can register multiple listeners
for the same event.

> #### Event Propagation

After all of the listeners for an event on an element have ran, the same event
is sent to the parent node, which also fires all of its listeners for that
event, and so on. There are two ways to change that:

- `e.stopPropagation()` will stop the event from bubbling up to the parents of
  the current element.
- `e.preventDefault()` will prevent the default event handlers from running.

### Getting More from Events

The `event` object passed to DOM event listeners have a lot of information.

### Handling the Differences Between Touch and Mouse Interfaces

We can detect touch devices with:

```javascript
'ontouchstart' in document.documentElement
```

On desktop, we can use the `mousedown` event as an alternative.

## Custom Events

> I personally think core.async channels are a much superior option.

Our application logic may require its own events (e.g. `coin_collected`). The
browser [has some support for that][1], which you should use, but in order to
explain de concepts we'll build our own here.

[1]: https://developer.mozilla.org/en-US/docs/Web/Events/Creating_and_triggering_events

Say we want to write:

```javascript
levelManager.on("loaded", (e) => {
  /* ... */
});
```

## Custom Event Listeners and Emitters

Building an event system is easy.

### EventEmitter: The Base Class

There are other options, but here we will make an `EventEmitter` where events
do not bubble up, event listeners only receive a single argument, and we try to
protect from memory leaks by limiting the number of listeners.

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
> I'm also not too fond of the `apply` call in `emit`.

### Events vs. Callbacks

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

## Custom Events

We want to build a single set of synthetic events to cover the differences
between touch and click devices:

- `down` covers `mousedown` and `touchstart`. The main difference here is that
  `touchstart` can have multiple locations in the case of a multi-touch screen.
- `up` covers `mouseup` and `touchend`, with the main difference being that
  `touchend` does not provide coordinates, so we have to track that.
- `move` covers `mousemove` and `touchmove`, but `mousemove` only when the
  button is pressed (i.e. we ignore hovering).

We also want `move` events to include the delta, as it is often useful.
Finally, we want to filter out unintentional movement to be able to detect taps
as opposed to short drags.

### Implementing InputHandlerBase

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

### Creating MouseInputHandler

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

### Creating TouchInputHandler

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
  el.addEventListener("touchstart", this._onDownDomEvent.bind(this));
  el.addEventListener("touchend", this._onUpDomEvent.bind(this));
  el.addEventListener("touchmove", this._onMoveDomEvent.bind(this));
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

## Advanced Input

### Drag-and-Drop

It's easy:

1. Pick: if the `down` event happens on a draggable object, start a drag.
2. Move: move the object along with `move` events.
3. Release: on `up`, release the object.

See [book code][2] for a full example.

[2]: https://github.com/Apress/pro-android-web-game-apps/blob/9e08321ca08e49246f51b1c88bc1ce1ab982aad8/9781430238195_sourcecode_chp05/code/04.drag_and_drop.html

### Pixel-Perfect Picking and Image Masks

### Composite Operations

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
