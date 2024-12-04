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
