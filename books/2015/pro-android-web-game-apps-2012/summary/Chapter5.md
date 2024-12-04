[Previous](./Chapter4.md) | [Next](./Chapter6.md)

<hr>

# Chapter 5 - Event Handling and User Input

## Browser Events

### Desktop Browser vs. Android Browser Input

### Using Events to Catch User Input

#### Event Object

#### Registering for Events: DOM Attributes

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

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
