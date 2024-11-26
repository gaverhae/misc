var GameRenderer = function() {
  function translated(ctx, left, top, op) {
    ctx.save();
    ctx.translate(left, top);
    op(ctx);
    ctx.restore();
  }
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
  }
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
  }
  function draw_token(ctx, size, color) {
    var radius = size * 0.4;
    var gx = size * 0.1;
    var gy = -size * 0.1;

    var gradient = ctx.createRadialGradient(
      gx, gy, size * 0.1,
      gx, gy, radius * 1.2);

    gradient.addColorStop(0, "yellow");
    gradient.addColorStop(1, color);
    ctx.fillStyle = gradient;

    ctx.beginPath();
    ctx.arc(0, 0, radius, 0, 2 * Math.PI, true);
    ctx.fill();
  }
  function draw_tokens(ctx, dims, tokens, get_color) {
    for (var col = 0; col < tokens.length; col++) {
      var column = tokens[col];
      var x = (col + 0.5) * dims.cell_size;
      for (var line = 0; line < column.length; line++) {
        var color = get_color[column[line]];
        var y = (dims.rows - line - 0.5) * dims.cell_size;
        translated(ctx, x, y, (ctx) => {
          draw_token(ctx, dims.cell_size, color);
        });
      }
    }
  }
  function render(ctx, width, height, game_data) {
    var dims = dimensions(width, height, game_data.rows, game_data.cols);

    translated(ctx, dims.left_offset, dims.top_offset, (ctx) => {
      draw_background(ctx, dims);
      draw_grid(ctx, dims);
      draw_tokens(ctx, dims, game_data.tokens, game_data.player_color);
    });
  }
  return {
    render
  };
}();
