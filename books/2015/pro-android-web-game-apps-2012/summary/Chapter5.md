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

### Creating TouchInputHandler

## Advanced Input

### Drag-and-Drop

### Pixel-Perfect Picking and Image Masks

### Composite Operations

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
