var GameModel = function() {
  function init(rows, cols) {
    return {
      rows: rows,
      cols: cols,
      tokens: Array.apply(null, Array(cols)).map(() => []),
      num_tokens: 0,
      player_color: ["red", "green"],
      current_player: 0,
      status: ["ongoing"]
    };
  }
  function move(state, column) {
    state.tokens[column].push(state.current_player);
    state.num_tokens = state.num_tokens + 1;
    state.status = update_status(state, column);
    state.current_player = 1 - state.current_player;
    return state;
  }
  function check_line(tokens, dir, col, row) {
    var p = tokens[col][row];
    var c = 0;
    var x = col + dir[0];
    var y = row + dir[1];
    while (tokens[x] && tokens[x][y] == p) {
      c = c + 1;
      x = x + dir[0];
      y = y + dir[1];
    }
    x = col - dir[0];
    y = row - dir[1];
    while (tokens[x] && tokens[x][y] == p) {
      c = c + 1;
      x = x - dir[0];
      y = y - dir[1];
    }
    return c >= 3;
  }
  function update_status(state, col) {
    var row = state.tokens[col].length - 1;
    var directions = [[0, 1], [1, 1], [1, -1], [1, 0]];
    for (var d = 0; d < directions.length; d++) {
      if (check_line(state.tokens, directions[d], col, row)) {
        return ["win", state.current_player];
      }
    }
    if (num_tokens = state.cols * state.rows) {
      return ["draw"];
    }
    return ["ongoing"];
  }
  return {
    init, move
  };
}();
