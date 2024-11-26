function Game(canvas) {
  this._boardRect = null;
  this._canvas = canvas;
  this._ctx = canvas.getContext("2d");
  this._boardModel = new BoardModel();
  this._boardRenderer = new BoardRenderer(this._ctx, this._boardModel);
  this.handleResize();
}

_p = Game.prototype;

_p.handleResize = function() {
  this._clearCanvas();
  this._boardRect = this._getBoardRect();
  this._boardRenderer.setSize(this._boardRect.x, this._boardRect.y, this._boardRect.cellSize);
  this._boardRenderer.repaint();
};

_p._getBoardRect = function() {
  var cols = this._boardModel.getCols();
  var rows = this._boardModel.getRows();
  var cellSize = Math.floor(Math.min(this._canvas.width / cols, this._canvas.height / rows));
  var boardWidth = cellSize * cols;
  var boardHeight = cellSize * rows;
  return {
    x: Math.floor((this._canvas.width - boardWidth) / 2),
    y: Math.floor((this._canvas.height - boardHeight) / 2),
    cellSize: cellSize
  };
};

_p.handleClick = function(x, y) {
  var column = Math.floor((x - this._boardRect.x) / this._boardRect.cellSize);
  var turn = this._boardModel.makeTurn(column);
  if (turn.status != BoardModel.ILLEGAL_TURN) {
    this._boardRenderer.drawToken(turn.x, turn.y);
  }

  if (turn.status == BoardModel.WIN) {
    alert((turn.piece == BoardModel.RED ? "red" : "green") + " won the match!");
    this._reset();
  }

  if (turn.status == BoardModel.DRAW) {
    alert("It is a draw!");
    this._reset();
  }
};

_p._reset = function() {
  this._clearCanvas();
  this._boardModel.reset();
  this._boardRenderer.repaint();
}

_p._clearCanvas = function() {
  this._ctx.fillStyle = "white";
  this._ctx.fillRect(0, 0, this._canvas.width, this._canvas.height);
}
