var GameModel = function() {
  function init(rows, cols) {
    return {
      rows: rows,
      cols: cols,
      tokens: [],
      player_color: ["red", "green"],
      current_player: 0,
      state: ["ongoing"]
    };
  }
  function move(state, column) {
  }
  return {
    init: init,
    move: move
  };
}();
