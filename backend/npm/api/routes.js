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
        .delete(deleteUserByID)
        .patch(updateUserByID),
    app.route("/users/:userID/inc")
        .get(incrementUserParkedCount)
    app.route("/locations")
        .post(insertLocation)
        // TODO: Remove get all locations, replace with near coordinates
        .get(getLocations),
    app.route("/locations/:locationID")
        .get(getLocationByID)
        .delete(deleteLocationByID)
        .patch(updateLocationByID),
    app.route("/locations/:longitude/:latitude/:radius")
        .get(getLocationsNearCoords),
    app.route("/locations/:southWestLong/:southWestLat/:northEastLong/:northEastLat")
        .get(getLocationsInBox),
    app.route("/park")
        .post(parkUser)
        .get(getParked),
    app.route("/park/:userID")
        .get(getUserParked)
        .delete(deleteUserParked),
    app.route("/areas")
        .post(insertArea)
};

// ---------------- User-related functions ---------------------

var User = Schema.Users;

function insertUser(req, res) {
    console.log("req body: " + util.inspect(req.body));
    var new_user = new User(req.body);

    new_user.save(function(err, user) {
        if (err) res.status(400).send(err);
        else res.json(user);

    });
}

function listUsers(req, res) {
    User.find({}, function(err, user) {
        if (err) res.status(400).send(err);
        else res.json(user);
    });
}

function getUserByID(req, res) {
   User.findById(req.params.userID, function(err, user) {
       if (err) res.status(400).send(err);
       else res.json(user);
   });
}
function deleteUserByID(req, res) {
    User.findByIdAndDelete(req.params.userID, function(err) {
        if (err) res.status(400).send(err);
        else res.json("User deleted successfully");
    });
}

function updateUserByID(req, res) {
    User.findByIdAndUpdate(req.params.userID, req.body, {new: true}, function(err, user) {
        if (err) res.status(400).send(err);
        else res.json(user);
    });
}

function incrementUserParkedCount(req, res) {
    User.findById(req.params.userID, function(err, user) {
        if (err) res.status(400).send(err);
        else {
            var userToUpdate = user;
            console.log(userToUpdate.numParked);
            User.findByIdAndUpdate(user._id, {numParked: userToUpdate.numParked + 1}, function(err, updateUser) {
                if (err) res.status(400).send(err);
                else res.json(updateUser);
            });
        }
    });
}

// ---------------- Location-related functions ---------------------

var Location = Schema.Locations;

function getLocations(req, res) {
    Location.find({}, function(err, location) {
        if (err) res.status(400).send(err);
        else res.json(location);
    });
}

function insertLocation(req, res) {
    var new_location = new Location(req.body);

    console.log(new_location);

    new_location.save(function(err, location) {
        if (err) res.status(400).send(err);
        else res.json(location);
    });
}

function getLocationByID(req, res) {
    Location.findById(req.params.locationID, function(err, location) {
        if (err) res.status(400).send(err);
        else res.json(location);
    })
}

function getLocationsNearCoords(req, res) {
    // Can't have multiple near queries in mongoDB, meaning search is limited to
    // coordinates near beginCoords unless work to aggregate two different 
    // queries is found necessary.
    Location.find({
        beginCoords: {
            $near: {
                $maxDistance: req.params.radius,
                $geometry: {
                    type: "Point",
                    coordinates: [req.params.longitude, req.params.latitude]
                }
            }
        }}, function (err, locations) {
            if (err) res.status(400).send(err);
            else res.json(locations);
        });
}

function getLocationsInBox(req, res) {
    Location.find({
        $or: [
            {beginCoords: {
                $geoWithin: {
                    $box: [
                        [req.params.southWestLong, req.params.southWestLat],
                        [req.params.northEastLong, req.params.northEastLat]
                    ]
                }
            }},
            {endCoords: {
                $geoWithin: {
                    $box: [
                        [req.params.southWestLong, req.params.southWestLat],
                        [req.params.northEastLong, req.params.northEastLat]
                    ]
                }
            }}
        ]
        
    }, function (err, locations) {
        if (err) res.status(400).send(err);
        else res.json(locations);
    });
}

function deleteLocationByID(req, res) {
    Location.findByIdAndDelete(req.params.locationID, function(err) {
        if (err) res.status(400).send(err);
        else res.json("Location Delete Successful");
    });

}

function updateLocationByID(req, res) {
    Location.findByIdAndUpdate(req.params.locationID, req.body, {new: true}, function(err, location) {
        if (err) res.status(400).send(err);
        else res.json(location);
    });
}

// ---------------- Parked-related functions -------------------

var Parked = Schema.Parked;

function parkUser(req, res) {

    var options = {
        upsert: true,
        new: true
    }

    Parked.findOneAndUpdate({user: req.body.user},
        req.body, options, function(err, parked)
    {
        if (err) res.status(400).send(err);
        else res.json(parked);
    });

}

function getParked(req, res) {
    Parked.find({}, function(err, parked) {
        if (err) res.status(400).send(err);
        else res.json(parked);
    });
}

function getUserParked(req, res) {
    Parked.findOne({user: req.params.userID}, function (err, parked) {
        if (err) res.status(400).send(err);
        else res.json(parked);
    });

}

function deleteUserParked(req, res) {
    Parked.findOneAndDelete({user: req.params.userID}, function(err, resp)
    {
        if (err) res.status(400).send(err);
        else if (resp != null) res.json("Location deleted successfully")
        else res.json(resp);
    });
}

// ---------------- Area-related functions ---------------------
var Area = Schema.Areas;

function insertArea(req, res) {
    console.log("req body: " + util.inspect(req.body));
    var new_area = new Area(req.body);

    console.log(req.body.coordinates);
    new_area.save(function(err, area){
        if (err) res.status(400).send(err);
        else res.json(area);
    });
}