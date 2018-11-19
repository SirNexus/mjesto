var express = require("express");
var bodyParser = require('body-parser');
var Routes = require("./api/routes.js");

app = express();
port = process.env.PORT || 3000;

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true}));

Routes(app);

app.listen(port);
console.log("Application started on port: " + port);