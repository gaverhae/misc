[Previous](./Chapter2.md) | [Next](./Chapter4.md)

<hr>

# Chapter 3

In this chapter, we make a "Connect Four" implementation.

The game is played on a 7-column, six-row board; each player has a color and
drops one piece on each turn, and the first player to align 4 pieces of their
color wins.

## HTML5 Game Skeleton

### The Standard Skeleton

### Forced Orientation

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
