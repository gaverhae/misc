function BoardModel(cols, rows) {
  this._cols = cols || 7;
  this._rows = rows || 6;
  this._data = [];
  this._currentPlayer = BoardModel.RED;
  this._totalTokens = 0;

  this.reset();
}

BoardModel.EMPTY = 0;
BoardModel.RED = 1;
BoardModel.GREEN = 2;

_p = BoardModel.prototype;

_p.reset = function() {
  this._data = []
  for (var i = 0; i < this._rows; i++) {
    this._data[i] = [];
    for (var j = 0; j < this._cols; j++) {
      this._data[i][j] = BoardModel.EMPTY;
    }
  }
  this._currentPlayer = BoardModel.RED;
  this._totalTokens = 0;
};

_p.getPiece = function(col, row) {
  return this._data[row][col];
};

_p.getCols = function() {
  return this._cols;
};

_p.getRows = function() {
  return this._rows;
};
