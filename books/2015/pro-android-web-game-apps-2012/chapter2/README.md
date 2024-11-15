# Chapter 2 - Graphics in the Browser: the Canvas Element

## The Anatomy of the Game

## Drawing Inside the Browser

## The Basic HTML Setup

## What is Canvas?

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

## Summary
