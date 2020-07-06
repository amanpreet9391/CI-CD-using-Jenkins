var express = require('express');
var app = express();

app.get('/', function (req, res) {
  res.send('Hello Guys! I hope you like my work :) This is a Green Deployment');
  
});

var server = app.listen(3000, function () {
  var host = server.address().address;
  var port = server.address().port;

  console.log('Example app listening at http://%s:%s', host, port);
});
