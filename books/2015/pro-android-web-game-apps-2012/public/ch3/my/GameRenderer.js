var GameRenderer = function() {
  function dimensions(width, height, rows, cols) {
    var cs = Math.floor(Math.min(width / cols, height / rows));
    var bw = cs * cols;
    var bh = cs * rows;
    return {
      left_offset: Math.floor((width - bw) / 2),
      top_offset: Math.floor((height - bh) / 2),
      board_width: bw,
      board_height: bh,
      cell_size: cs,
      rows: rows,
      cols: cols
    };
  }
  function draw_background(ctx, dims) {
    var gradient = ctx.createLinearGradient(0, 0, 0, dims.board_height);
    gradient.addColorStop(0, "#fffbb3");
    gradient.addColorStop(1, "#f6f6b2");
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, dims.board_width, dims.board_height);

    ctx.strokeStyle = "#dad7ac";
    ctx.fillStyle = "#f6f6b2";

    var w = dims.board_width;
    var h = dims.board_height;

    ctx.beginPath();
    ctx.moveTo(1/6 * w, h);
    ctx.bezierCurveTo( 9/6 * w, -1/6 * w,
                      -3/6 * w, -1/6 * w,
                       5/6 * w, h);
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(1/6 * w, 0);
    ctx.bezierCurveTo( 9/6 * w, h + 1/6 * w,
                      -3/6 * w, h + 1/6 * w,
                       5/6 * w, 0);
    ctx.fill();
  };
  function draw_grid(ctx, dims) {
    ctx.beginPath();
    var top = 0.5;
    var left = 0.5;
    var bottom = dims.rows * dims.cell_size + 0.5;
    var right = dims.cols * dims.cell_size + 0.5;
    for (var i = 0; i <= dims.cols; i++) {
      var x = i * dims.cell_size + 0.5;
      ctx.moveTo(x, top);
      ctx.lineTo(x, bottom);
    }
    for (var j = 0; j <= dims.rows; j++) {
      y = j * dims.cell_size + 0.5;
      ctx.moveTo(left,  y);
      ctx.lineTo(right, y);
    }

    ctx.strokeStyle = "#CCC";
    ctx.stroke();
  };
  function draw_token(ctx, x, y, size) {
  }
  function render(ctx, width, height, game_data) {
    ctx.save();
    var rows = game_data.length;
    var cols = game_data[0].length;
    var dims = dimensions(width, height, rows, cols);
    ctx.translate(dims.left_offset, dims.top_offset);

    draw_background(ctx, dims);
    draw_grid(ctx, dims);

    for (var i = 0; i < cols; i++) {
      for (var j = 0; j < rows; j++) {
        draw_token(ctx, i, j, dims.cs);
      }
    }
    ctx.restore();
  }
  return {
    render: render
  };
}();
