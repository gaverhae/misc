[Previous](./Chapter2.md) | [Next](./Chapter4.md)

<hr>

# Chapter 3

In this chapter, we make a "Connect Four" implementation.

The game is played on a 7-column, six-row board; each player has a color and
drops one piece on each turn, and the first player to align 4 pieces of their
color wins.

## HTML5 Game Skeleton

If our game is meant to be a full-screen canvas, we want to control as much of
the screen as we want with our canvas. This requires disabling the default
browser behaviour of scrolling and resizing content, and scaling images.

See [listing 3.6] for the new version of our skeleton, which accomplishes that.

[listing 3.6]: http://127.0.0.1/ch3/listing-3-1.html

> Note that `device-densitydpi` has since been removed from Android browsers.
> It never did anything in other browsers. Also, I think these days using
> `100mvh` and `100mvw` makes more sense than `100%` for resizing browsers on
> mobile.

For some games, it may make sense to "force" the orientation to either portrait
or landscape. It's not possible to prevent the browser chrome from rotating,
but one can add a message to the game itself asking to change the orientation.

Orientation changes can be detected through the `orientationchange` event, and
the current orientation can be retrieved from `window.orientation` (as an angle
value). An alternative to the above message is to "counter" the rotation with a
canvas rotation, along the lines of:

```javascript
function reorient() {
  var angle = window.orientation;
  if (angle) {
    var rot = -Math.PI * angle / 180;
    ctx.translate(angle == -90 ? canvas.width : 0,
                  angle == 90 ? canvas.heigth : 0);
    ctx.rotate(rot);
  }
}
```

> `orientationchange` and `window.orientation` have also since been removed,
> though there is now a [screen orientation API] standard.

[screen orientation API]: https://developer.mozilla.org/en-US/docs/Web/API/Screen_Orientation_API

If you can make your game work in all orientations, that's probably better.

## Game Architecture

The basic game loop will be:

1. Display an empty board.
2. User taps a column, if that's a valid move we add a piece.
3. If the last move was a win or draw, go to 5.
4. Go to 2.
5. Display a message and offer the option to reset the board.

We break down this logic in the following classes:

- `Game` orchestrates.
- `BoardModel` maintains the state of the game.
- `BoardRenderer` displays the game, mostly reusing [Chapter 2] code.

[Chapter 2]: ./Chapter2.md

## Making the Game

We start from [listing 3.6] and add a JS file per class. The final result is
[here][final-js].

[final-js]: http://127.0.0.1:8080/ch3/js.html

### Rendering the Board

#### Constructor

#### Working with different Screen Sizes

#### JSDoc Comments

#### Rendering Board

### Game State and Logic

#### Making Moves

#### Win Condition

### Wiring Components Together: The Game Class

### ADding the Game to the HTML Skeleton

## Summary

<hr>

[Previous](./Chapter2.md) | [Next](./Chapter4.md)
