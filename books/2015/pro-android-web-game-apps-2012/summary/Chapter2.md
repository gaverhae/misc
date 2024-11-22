[Previous](./Chapter1.md) | [Next](./Chapter3.md)

<hr>

# Chapter 2 - Graphics in the Browser: the Canvas Element

## The Anatomy of the Game

Graphics are important, but not sufficient. You also need good user
interactions, and they need to work as expected: players are a lot more
forgiving with the occasional graphical glitch than with a flawed controller.

Also, the game logic needs to be fun.

## Drawing Inside the Browser

There are three ways to draw in a browser:

- manipulating DOM elements,
- manipulating SVG,
- drawing on a canvas.

This book focuses on canvas: DOM elements are fine for very simple games, but
coordinating animations is difficult; and generating SVG is hard because XML.

> I think a lot of the reasons given against SVG and DOM in this book are much
> weaker now in a post-React world, but ultimately they still hold enough to
> make a strong case for canvas.

## The Basic HTML Setup

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <style></style>
  <script>
    function init() {
    }
  </script>
</head>
<body>
</body>
</html>
```

## What Is Canvas?

Canvas is a stateful element, like the old Paint app. In order to change the
display, you have to paint over what's already there.

Simple example:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <style></style>
  <script>
    function init() {
      var canvas = document.getElementById('mainCanvas');
      var ctx = canvas.getContext('2d');
      ctx.clearRect(0, 0, 300, 300);
      ctx.fillStyle = "lightgray";
      ctx.fillRect(10, 10, 50, 50);
    }
  </script>
</head>
<body onload="init()">
  <canvas id="mainCanvas" width="300px" height="300px"></canvas>
</body>
</html>
```

> If you have the server running (`server start`), this snippet is displayed at
> [/ch2/listing-2-2.html](http://127.0.0.1:8080/ch2/listing-2-2.html).

### The Context

The context is the brush we use to draw on the canvas. There are other types of
contexts, and a way to get at the underlying bitmap, but we'll focus on `'2d'`
for now.

### The Coordinate System

The coordinate system of the `2d` context is a Cartesian system with its origin
at the top left of the canvas element, a unit of 1 pixel, its $x$ axis goes to
the right, and its $y$ axis goes to the bottom.

The origin is the point on the top left of the top-left pixel, coordinate $(1,
1)$ is the bottom right of that pixel. The center of the pixel is $(0.5, 0.5)$.

## Drawing Shapes

The `2d` context has a separate API for rectangles.

### Rectangles

There are two methods for drawing a rectangle, depending on whether we want to
draw an outline (`drawRect`) or a filled shape (`fillRect`). They both take
four arguments: the top-left corner of the rectangle, its width, and its
height.

> See [listing 2.3] and [listing 2.4] for more examples.

[listing 2.3]: http://127.0.0.1:8080/ch2/listing-2-3.html
[listing 2.4]: http://127.0.0.1:8080/ch2/listing-2-4.html

### Paths

For non-rectangular shapes, the process has one more step: one must first
define a _path_, and then either fill it or stroke it (or both).

There are four ways to define paths: lines, arcs, quadratic curves, or Bézier
curves.

To illustrate the overall process, here is the code to draw a simple line:

```javascript
ctx.beginPath();
ctx.moveTo(50, 50); // moves the cursor; not defining anything yet
ctx.lineTo(120, 100); // begins tracing a path; we could have more steps here
ctx.strokeStyle = "#000"; // set color; still not displaying anything
ctx.stroke(); // this makes the black line from 50, 50 to 120, 100 appear
```

We use this to draw a grid in [listing 2.5]:

```javascript
      var cellSize = 40;
      ctx.beginPath();

      for (var i = 0; i < 8; i++) {
        ctx.moveTo(i * cellSize + 0.5, 0);
        ctx.lineTo(i * cellSize + 0.5, cellSize * 6);
      }

      for (var j = 0; j < 7; j++) {
        ctx.moveTo(0, j * cellSize + 0.5);
        ctx.lineTo(cellSize * 7, j * cellSize + 0.5);
      }

      ctx.lineWidth = 1;
      ctx.strokeStyle = "#989681";
      ctx.stroke();
```

[listing 2.5]: http://127.0.0.1:8080/ch2/listing-2-5.html

Note that calling `closePath` is not required; it will draw a line from the
last point of a subpath to the first, which is not always desired.

Arcs are drawn directly, without having to move the cursor. The `arc` method
takes six arguments: $x$ and $y$ of the center, the radius, the start and end
angles in radians, and a Boolean for direction (`false` is horlogic, `true` is
trigonometric; defaults to false).

For example, drawing a circle could be done with:

```javascript
var x = 90;
var y = 70;
var radius = 50;
ctx.beginPath();
ctx.arc(x, y, radius, 0, 2*Math.PI, false);
ctx.fillStyle = "darkgoldenrod";
ctx.lineWidth = 5;
ctx.strokeStyle = "black";
ctx.fill();
ctx.stroke();
```

See [listing 2-6] for a more complete example.

[listing 2.6]: http://127.0.0.1:8080/ch2/listing-2-6.html

The `2d` context supports two types of Bézier curves: quadratic
(`.quadraticCurveTo(cp_x, cp_y, x, y)`) and cubic (`.bezierCurveTo(cp1_x,
cp1_y, cp2_x, cp2_y, x, y)`). In both cases, the start point of the curve is
the current position of the brush (typically defind by a `moveTo` or other
preceding drawing), the last position (`x, y`) is the end point of the curve,
and the one or two additional points are "control points" in the mathematical
sense of Bézier curves.

See [listing 2.7] for an example use.

[listing 2.7]: http://127.0.0.1:8080/bh2/listing-2-7.html

### Subpaths

Names are a bit misleading here. A path is a set of sub-paths. When `beginPath`
is called, a new, empty path is created and the previous path is discarded,
including all of its sub-paths. When `stroke` or `fill` is called, the current
path draws all of its sub-paths on the screen.

A new sub-path is created by calling `moveTo`. The `closePath` function closes
the "geometrical" path of the current sub-path, but does not close the current
path; essentially it draws a line from the current position to the position of
the last `moveTo`.

[Listing 2.8] illustrates this behaviour: calling `closePath` does not close
the path, as shown by `stroke` called after the two `closePath` calls still
drawing both shapes.

[Listing 2.8]: http://127.0.0.1:8080/ch2/listing-2-8.html

Usually, one would create a new path per figure, but when drawing a lot of
figures with the same stroke and fill style, creating a single path with many
sub-paths can be faster.

## Strokes and Fills

Stroke is the border, fill is the interior. `fillStyle` and `strokeStyle`
accept the same argument, which can be in one of three form:

- Solid colors: any valid CSS color (name, hex code, rgb or rgba function).
- Gradients, which are JavaScript objects that mirror the CSS gradient property.
- Patterns, which repeat a given image.

Gradients can be linear or radial. We first need to create a gradient object
with `ctx.createLinearGradient` or `ctx.createRadialGradient`; linear takes two
points as four coordinates, and radial takes two circles as two times two
coordinates and a radius.

Once the gradient object is created, we add color stops wth the `addColorStop`
method, which takes a position (between 0 and 1) and a color (per solid colors
above). Once all of the stops have been added, the gradient object can be
assigned to ther `fillStyle` or `strokeStyle`.

[Listing 2.12] uses a radial gradient to add lighting to the pieces. (It is
otherwise identical to [listing 2.6].)

[Listing 2.12]: http://127.0.0.1:8080/ch2/listing-2-12.html

Finally, patterns allow us to take an image and repeat it. The image needs to
be fully loaded inside the browser before the canvas drawing calls. A simple
way to deal with that is with the `onload` method on the `Image` object, but
a better approach will be discussed in [Chapter 4](./Chapter4.md).

The `ctx.createPattern(img, strat)` call takes two arguments: an `Image`
object, which must have finished loading, and a "repeat strategy" in the same
format as the `background-repeat` CSS rule.

[Listing 2.14] shows an example of using a pattern and a simple way to wait for
the image to be loaded.

The context remembers the stroke and fill settings until they are changed.

## Context State and Transformations

### Translate

### Scale

### Rotate

### Stacking Transformations

### Context State

### Context Transformations in the Sample Project

## The Sample Game Project Result

<hr>

[Previous](./Chapter1.md) | [Next](./Chapter3.md)
