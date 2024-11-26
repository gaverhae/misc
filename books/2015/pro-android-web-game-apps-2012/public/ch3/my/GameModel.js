var GameModel = function() {
  function init(rows, cols) {
    return {
      rows: rows,
      cols: cols,
      tokens: Array.apply(null, Array(cols)).map(() => []),
      player_color: ["red", "green"],
      current_player: 0,
      state: ["ongoing"]
    };
  }
  function move(state, column) {
    if (column < 0 || column >= state.cols) {
      return ["failure", "invalid column"];
    }
    if (state.tokens[column].length == state.cols) {
      return ["failure", "column full"];
    }
    state.tokens[column].push(state.current_player);
    state.current_player = 1 - state.current_player;
    return ["success", state];
  }
  return {
    init: init,
    move: move
  };
}();
