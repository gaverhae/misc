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

### Pixel-Perfect Picking and Image Masks

### Composite Operations

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
