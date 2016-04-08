$(document).ready(function() {
  $('button').click(function() {
    // clear old output
    $('.output').removeClass('output-show');
    var out = $('#output');
    out.html("");

    var v = $('input').val();
    if (!v) {
      return;
    }

    var url = window.location.protocol + "//" + window.location.host;
    $('#output-url').html(url + "/" + v);

    $.ajax("/" + v, {
      error: function(err) {
        out.html("error: " + JSON.stringify(err));
        $('.output').addClass('output-show');
      },
      success: function(data) {
        out.html(JSON.stringify(data));
        $('.output').addClass('output-show');
      }
    });
  });
});
