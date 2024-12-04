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
