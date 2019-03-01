var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var UserSchema = new Schema({
    name: {
        type: String,
        required: "Please enter the name of the person"
    },
    created_date: {
        type: Date,
        default: Date.now()
    },
});

var LocationSchema = new Schema({
    type: {
        type: String,
        enum: ["Point"],
        required: "Location must be of type 'Point'"
    },
    created_date: {
        type: Date,
        default: Date.now()
    },
    restriction: {
        type: String,
        enum: ["restricted", "unlimited", "limited"],
        required: "Location must have a restriction",
        validate: [validateLocation, "limit must be defined for limited restriction"]
    },
    limit: {
        type: Number,
    },
    coordinates: {
        type: [Number],
        required: "Please enter a longitude, latitude",
        validate: [validateCoordinates, "Coordinates not valid [(longitude, latitude)]"]
    }
});

var AreaSchema = new Schema({
    type: {
        type: String,
        enum: ['Polygon'],
        required: "Area must be of type 'Polygon"
    },
    coordinates: {
        type: [[Number]],
        required: "Please enter a set of Geo Coordinates",
        validate: [validateArea, "Coordinates are not of format [(long, lat)] required "]
    }
})

function validateLocation (val) {
    if (val == "limited" && this.limit == undefined) {
        return false;
    } else if (val != "limited") {
        this.limit = undefined;
        return true;
    }
    return true
}

function validateCoordinates (val) {
    if (val.length != 2) {
        return false;
    }
    if (Math.abs(val[0]) > 180 || Math.abs(val[1]) > 90) {
        return false;
    }
    return true;
}

function validateArea(val) {
    console.log("validate area value: " + val);

    val.forEach(function(element) {
        console.log(element);
    });
}

module.exports = {
    Users: mongoose.model("Users", UserSchema),
    Locations: mongoose.model("Locations", LocationSchema),
    Areas: mongoose.model("Areas", AreaSchema)
}