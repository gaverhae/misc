var Game = function() {
  function init(canvas_id) {
    var model = GameModel.init(6, 7);
    var dims;
    var canvas = ResizeableCanvas.init("mainCanvas", (ctx, width, height) => {
      dims = GameRenderer.dimensions(width, height, model.rows, model.cols);
      GameRenderer.render(ctx, dims, model);
    });
    function handle_click(x, y) {
      console.log(x, y);
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
