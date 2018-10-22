// var mongoClient = require('mongodb').mongoClient;
var express = require("express");
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var util = require('util');
// var url = "mongodb://localhost:27017";
var Schema = require("./api/schema.js");
app = express();
port = process.env.PORT || 3000;

mongoose.connect('mongodb://localhost:27017')
var User = mongoose.model("Users");

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true}));

function helloWorld() {
    console.log("Hello world!\n");
}

function mongoInsert(req, res) {
    console.log("In post function!");


    console.log("req body: " + util.inspect(req.body));
    var new_user = new User(req.body);

    new_user.save(function(err, task) {
        if (err) res.send(err);
        res.json(task);

    });
}

function mongoGetUsers(req, res) {
    User.find({}, function(err, task) {
        if (err) res.send(err);
        res.json(task);
    })
}

function mongoGetByID(req, res) {
   User.findById(req.params.userID, function(err, task) {
       if (err) res.send(err);
       res.json(task);
   }) 
}
function mongoDeleteByID(req, res) {
    User.findByIdAndDelete(req.params.userID, function(err) {
        if (err) res.send(err);
        res.json("User deleted successfully");
    })
}

app.route("/users")
    .post(mongoInsert)
    .get(mongoGetUsers);
app.route("/users/:userID")
    .get(mongoGetByID)
    .delete(mongoDeleteByID);
app.listen(port);
console.log("Application started on port: " + port);

helloWorld();
// mongoInsert();