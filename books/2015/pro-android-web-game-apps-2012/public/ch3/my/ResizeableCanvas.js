var ResizeableCanvas = function() {
  function resize_canvas(canvas, cb) {
    var ctx = canvas.getContext("2d");
    canvas.width = document.width || document.body.clientWidth;
    canvas.height = document.height || document.body.clientHeight;
    cb(ctx, canvas.width, canvas.height);
  }
  function init(id, callback) {
    var canvas = document.getElementById(id);
    resize_canvas(canvas, callback);
    window.addEventListener("resize", function() {
      resize_canvas(canvas, callback);
    });
    return canvas;
  }
  return {
    init: init
  };
}();
