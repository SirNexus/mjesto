console.log("In script.js")

document.getElementById("postButton").addEventListener("click", function(event) {
});

var map;
var marker;
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 44.5646, lng: -123.2620},
        zoom: 14
    });

    google.maps.event.addListener(map, "click", function (event) {
        console.log(event.latLng);
        console.log(event.latLng.lat());
        console.log(event.latLng.lng());

        if (marker == undefined) {
            marker = new google.maps.Marker({position: event.latLng, map: map});
        } else {
            marker.setPosition(event.latLng);
            console.log(marker);
        }
    });

    document.getElementById("postButton").addEventListener("click", populateMap)
    document.getElementById("createSpot").addEventListener("click", createSpot)

};

function populateMap(event) {
    // Get parking locations from remote REST API
    var returnText = fetch("http://localhost/locations")
        .then(data=>data.json())
        .catch(error=>{console.log("Err: " + error)});

    
    returnText.then(function(jsonData) {
        // console.log(jsonData);
        jsonData.forEach(function(spot) {
            // console.log(spot);
            let coordinates = spot.coordinates;
            let tempMarker = new google.maps.Marker({position:coordinates, map:map});
            google.maps.event.addListener(tempMarker, "click", function(event){
                console.log(event);
            });
        });

    });
}

function createSpot(event) {

    if (marker == undefined){
        console.log("No spot defined");
        return;
    }

    var newSpot = {
        type: "Point",
        coordinates: {
            lat: marker.position.lat(),
            lng: marker.position.lng()
        }
    }

    console.log(newSpot);

    var returnText = fetch("http://localhost/locations", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(newSpot)
    }); 
    


}
