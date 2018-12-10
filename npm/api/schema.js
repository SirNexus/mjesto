var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var UserSchema = new Schema({
    name: {
        type: String,
        required: "Please enter the name of the person"
    },
    created_date: {
        type: Date,
        default: Date.now
    },
});

var LocationSchema = new Schema({
    type: {
        type: String,
        enum: ['Point'],
        required: "Location must be of type 'Point'"
    },
    coordinates: {
        lat: {
            type: Number,
            required: "Please enter a latitude",
        },
        lng: {
            type: Number,
            required: "Please enter a longitude"
        }
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

function validateLocation(val) {
    console.log("validate coordinates value: " + val);

    if (val.length != 2) {
        return false
    }
    return true
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