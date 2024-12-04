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

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
