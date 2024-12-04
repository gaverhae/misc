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
