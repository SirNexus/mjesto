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
})

module.exports = mongoose.model("Users", UserSchema);