var Schema = require("./schema.js");
var mongoose = require('mongoose');
var util = require('util');

mongoose.connect('mongodb://localhost:27017', {useNewUrlParser: true});

module.exports = app => {
    app.route("/users")
        .post(insertUser)
        .get(listUsers),
    app.route("/users/:userID")
        .get(getUserByID)
        .delete(deleteUserByID),
    app.route("/locations")
        .post(insertLocation)
        .get(getLocations)
    app.route("/locations/:locationID")
        .get(getLocationByID)
        .delete(deleteLocationByID),
    app.route("/areas")
        .post(insertArea)
};

// ---------------- User-related functions ---------------------

var User = Schema.Users;

function insertUser(req, res) {
    console.log("req body: " + util.inspect(req.body));
    var new_user = new User(req.body);

    new_user.save(function(err, user) {
        if (err) res.send(err);
        res.json(user);

    });
}

function listUsers(req, res) {
    User.find({}, function(err, user) {
        if (err) res.send(err);
        res.json(user);
    });
}

function getUserByID(req, res) {
   User.findById(req.params.userID, function(err, user) {
       if (err) res.send(err);
       res.json(user);
   });
}
function deleteUserByID(req, res) {
    User.findByIdAndDelete(req.params.userID, function(err) {
        if (err) res.send(err);
        res.json("User deleted successfully");
    });
}

// ---------------- Location-related functions ---------------------

var Location = Schema.Locations;

function getLocations(req, res) {
    console.log("req body: " + util.inspect(req.body));
    Location.find({}, function(err, location) {
        if (err) res.send(err);
        res.json(location);
    });
}

function insertLocation(req, res) {
    console.log("req body: " + util.inspect(req.body));    
    var new_location = new Location(req.body);

    new_location.save(function(err, location) {
        if (err) res.send(err);
        res.json(location);
    });
}

function deleteLocationByID(req, res) {
    console.log("req body: " + util.inspect(req.body));

    Location.findByIdAndDelete(req.params.locationID, function(err) {
        if (err) res.send(err);
        res.json("Location delete successfully");
    });

}

function getLocationByID(req, res) {
    console.log("req body: " + util.inspect(req.body));

    Location.findById(req.params.locationID, function(err, location) {
        if (err) res.send(err);
        res.json(location);
    })
}

// ---------------- Area-related functions ---------------------
var Area = Schema.Areas;

function insertArea(req, res) {
    console.log("req body: " + util.inspect(req.body));
    var new_area = new Area(req.body);

    console.log(req.body.coordinates);
    new_area.save(function(err, area){
        if (err) res.send(err);
        res.json(area);
    });
}