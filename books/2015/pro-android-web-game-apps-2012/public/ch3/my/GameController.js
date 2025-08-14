var GameController = function() {
  function move_from_click(dims, x, y) {
    if ( x < dims.left_offset
      || x > dims.left_offset + dims.board_width
      || y < dims.top_offset
      || y > dims.top_offset + dims.board_height) {
      return -1;
    }
    return Math.floor((x - dims.left_offset) / dims.cell_size);
  }
  return {
    move_from_click
  };
}();
