var Game = function() {
  function init(canvas_id) {
    var model = GameModel.init(6, 7);
    var dims;
    var canvas = ResizeableCanvas.init("mainCanvas", (ctx, width, height) => {
      dims = GameRenderer.dimensions(width, height, model.rows, model.cols);
      GameRenderer.render(ctx, dims, model);
    });
    function handle_click(x, y) {
      var col = GameController.move_from_click(dims, x, y);
      if (col != -1) {
        model = GameModel.move(model, col);
      }
      switch (model.status[0]) {
        case "win":
          alert("Player " + model.current_player + " wins!");
          model = GameModel.init(6, 7);
          break;
        case "draw":
          alert("It's a draw!");
          model = GameModel.init(6, 7);
          break;
        case "ongoing":
          break;
      }
      GameRenderer.render(canvas.getContext("2d"), dims, model);
    }
    canvas.addEventListener("click", function(e) {
      handle_click(e.x, e.y);
      e.stopPropagation();
      e.preventDefault();
    });
  }
  return {
    init
  };
}();
