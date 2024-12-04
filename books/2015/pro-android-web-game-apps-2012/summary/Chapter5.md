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

## Custom Events

### Implementing InputHandlerBase

### Creating MouseInputHandler

### Creating TouchInputHandler

## Advanced Input

### Drag-and-Drop

### Pixel-Perfect Picking and Image Masks

### Composite Operations

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
