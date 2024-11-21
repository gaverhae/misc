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

## Strokes and Fills

### Solid Colors

### Gradients

### Patterns

## Context State and Transformations

It is possible to apply transformations to the canvas: translation, rotation,
and scaling.

They behave as expected: `ctx.translate(dx, dy)` moves the coordinate axes `dx`
pixels to the right and `dy` pixels down, so the top-left of the canvas is then
`(-dx, -dy)`; `ctx.scale(mx, my)` multiplies all coordinates, so `mx` above 1
"stretches" drawings and below one "shinks" them (negative values are allowed
and work as expected for multiplication); and since we're in `2d`,
`ctx.rotate(rad)` takes a single argument and rotates everything that many
radians clockwise around the $z$ axis.

Transformations stack, so for example to rotate around another point than the
origin one can chain a translation, a rotation, and another translation.
Stacked transformations are "free" in terms of performance: the `2d` context
stores the final transformation matrix, not a chain of individual
transformations. It is possible to manipulate the transformation matrix
directly with either `ctx.transform(a, b, c, d, e, f)` or `ctx.setTransform(a,
b, c, d, e, f)`. The transformation matrix is a 3x3 matrix that can be
expressed by 6 numbers because the last line is always `[0, 0, 1]` (because we
cannot rotate around the $x$ or $y$ axes).

We'll take a closer look at the transformation matrix in [Chapter
8](./Chapter8.md).

The _context state_ is a representation of all the properties we have set on
the context: stroke style, transforms, and so on. It is sometimes useful to
save it and later restore it. The API for that is `ctx.save()` and
`ctx.restore()`. Neither takes any argument, but the canvas keeps track of an
internal stack of saved states, so nested calls would work as expected.

[Listing 2.20] uses these techniques to clean up the project code we've got so
far.

[Listing 2.20]: http://127.0.0.1:8080/ch2/listing-2-20.html

> [/ch2/final.html] is a somewhat cleaned-up version of that, because I know
> about functions.

[/ch2/final.html]: http://127.0.0.1:8080/ch2/final.html

<hr>

[Previous](./Chapter1.md) | [Next](./Chapter3.md)
