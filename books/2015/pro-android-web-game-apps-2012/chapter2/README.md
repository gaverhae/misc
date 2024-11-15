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

> If you have the server running (`server start`), this snippet is displayed at
> [/listing-2-2.html](http://127.0.0.1:8080/listing-2-2.html).

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

### The Context

### The Coordinate System

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
