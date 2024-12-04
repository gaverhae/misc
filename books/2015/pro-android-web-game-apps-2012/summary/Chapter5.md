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

## Advanced Input

### Drag-and-Drop

### Pixel-Perfect Picking and Image Masks

### Composite Operations

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
