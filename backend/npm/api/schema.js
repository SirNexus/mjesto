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
    },
    endDate: {
        type: String
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
        enum: ["no restriction", "limited", "restricted"],
        required: "Location must have a restriction"
    },
    metered: {
        type: Boolean,
        validate: [validateLimitedAttr, "If restriction is limited, metered must be specified"]
    },
    allDay: {
        type: Boolean,
        validate: [validateDay, "if restriction is not restricted, all_day must be specified"]
    },
    limit: {
        type: String,
        validate: [validateLocation, "limit must be defined for limited restriction"]
    },
    restrictionStart: {
        type: String,
        validate: [validateRestrictionTime, "restriction start must be specified when all_day is false"]
    },
    restrictionEnd: {
        type: String,
        validate: [validateRestrictionTime, "restriction end must be specified when all_day is false"]
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

function validateLocation(val) {
    if (val == undefined && this.restriction == "limited") {
        return false;
    } else if (this.restriction != "limited") {
        this.limit = undefined;
        return true;
    }
    return true
}

function validateCoordinates(val) {
    if (val.length != 2) {
        return false;
    }
    if (Math.abs(val[0]) > 180 || Math.abs(val[1]) > 90) {
        return false;
    }
    return true;
}

function validateLimitedAttr(val) {
    if (val == undefined && this.restriction == "limited") {
        return false;
    }
    return true;
}

function validateDay(val) {
    if (val == undefined && this.restriction != "restricted") {
        return false
    }
    return true;
}

function validateRestrictionTime(val) {
    console.log("restriction: " + val);
    if (val == undefined && this.all_day == false) {
        return false;
    }
    return true;
}

module.exports = {
    Users: mongoose.model("Users", UserSchema),
    Locations: mongoose.model("Locations", LocationSchema),
    Parked: mongoose.model("Parked", ParkedSchema),
}