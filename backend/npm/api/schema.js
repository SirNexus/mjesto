var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var UserSchema = new Schema({
    name: {
        type: String,
        required: "Please enter the name of the person"
    },
    numParked: {
        type: Number,
        default: 0
    },
    created_date: {
        type: Date,
        default: Date.now()
    }
});

var ParkedSchema = new Schema({
    user: {
        type: Schema.Types.ObjectId,
        ref: 'UserSchema',
        required: true
    },
    location: {
        type: Schema.Types.ObjectId,
        ref: 'LocationSchema',
        required: true
    }
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
        enum: ["no restriction", "unlimited", "limited"],
        required: "Location must have a restriction",
        validate: [validateLocation, "limit must be defined for limited restriction"]
    },
    limit: {
        type: Number,
    },
    beginCoords: {
        type: [Number],
        required: "Please enter a longitude, latitude",
        index: "2dsphere",
        validate: [validateCoordinates, "Coordinates not valid [(longitude, latitude)]"]
    },
    endCoords: {
        type: [Number],
        required: "Please enter a longitude, latitude",
        index: "2dsphere",
        validate: [validateCoordinates, "Coordinates not valid [(longitude, latitude)]"]
    }
});

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
    Parked: mongoose.model("Parked", ParkedSchema),
}