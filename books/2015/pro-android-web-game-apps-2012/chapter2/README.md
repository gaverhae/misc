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
> [/listing-2-2.html](http://127.0.0.1:8080/listing-2-2.html).

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

### Rectangles

### Paths

### Subpaths

## Strokes and Fills

### Solid Colors

### Gradients

### Patterns

## Context State and Transformations

### Translate

### Scale

### Rotate

### Stacking Transformations

### Context State

### Context Transformations in the Sample Project

## The Sample Game Project Result
