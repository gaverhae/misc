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

## Advanced Input

### Drag-and-Drop

### Pixel-Perfect Picking and Image Masks

After the `down` event, we have the coordinates of the selected pixel. We can
ask the canvas for the color of that pixel with:

```javascript
ctx.getImageData(x, y, 1, 1).data
```

which returns a 4-element array of RGBa. We can repaint a virtual canvas with a
mask of each of our sprites, assigning a specific, single color to each mask,
and then check the color of that pixel on that virtual canvas to find out which
element should be picked.

> In this context, a "virtual" canvas is one that is not mounted into the DOM. We
> can create such a canvas with:
>
> ```javascript
> var virtual_canvas = document.createElement("canvas");
> ```
>
> This is in all respects a "real" canvas that just happens not to be drawn on
> the screen. We can set its size with `virtual_canvas.height = 5` and get its
> context with `virtual_canvas.getContext("2d")` etc.

### Composite Operations

## Simulating Joystick

## Summary

<hr>

[Previous](./Chapter4.md) | [Next](./Chapter6.md)
