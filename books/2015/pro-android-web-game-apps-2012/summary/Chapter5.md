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
