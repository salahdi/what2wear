var what2wear = what2wear || {};

what2wear.getUrlArgs = function() {
  var args = new Object();    
  var params = window.location.href.split('?');

  if (params.length > 1) {
    params = params[1];
    var pairs = params.split("&");
    for ( var i = 0; i < pairs.length; i++) {
      var pos = pairs[i].indexOf('=');
      if ( pos == -1 ) continue;
      var argname = pairs[i].substring(0, pos);
      var value = pairs[i].substring(pos + 1);
      value = value.replace(/\+/g, " ");
      args[argname] = value;
    }
  }
  return args;
}

what2wear.textTruncate = function(text, length) {
  if (text.length > length) {
    truncatedText = text.substring(0, length);
    truncatedText = truncatedText.replace(/\w+$/, '');
    truncatedText = [truncatedText, ' ...'].join('');  
    return truncatedText;
  } else {
    return text;
  }
}

what2wear.getTitle = function() {
  return what2wear.URL_ARGS.title;
};

what2wear.getCurrentPage = function() {
  var page = what2wear.URL_ARGS.page;

  if (!page) {
    page = 1;
  }
  return parseInt(page);
}
