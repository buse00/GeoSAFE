/* script.js
/** author: Thibaut Vercueil
*/

//Ressources
var map;
var count = 0;


var circleOptions = {};
var cityCircle = {};
var markerAlert = {};
var marker_rescuers = {};

var selectedItem = 0;
var selectedAdress = {};
var selectedLat  = {};
var selectedLng = {};

var pin;
var pin_infowindow;
var bufferLat, bufferLng;

pin_infowindow = pin_infowindow = new google.maps.InfoWindow({
      content: null
  });


pin = new google.maps.Marker({
  map: map,
  title:"Alerte"
});


marker_rescuer = new google.maps.Marker({
  map: map,
  title:"Secouriste"
});

var iFrequency = 1000; // expressed in miliseconds
var myInterval = 1;

var socket = io.connect('http://etherluminifer.ddns.net:5000');



var geocoded_adress;
//declaring array of markers
var markers_salvers = [];

//Style:
map_style_default = [{
  "featureType": "all",
  "elementType": "all",
  "stylers": [{
    "invert_lightness": false
  }]
}];

map_style_dark = [{
  "featureType": "all",
  "elementType": "all",
  "stylers": [{
    "invert_lightness": true
  }]
}];


//Initialize function
function initialize() {


  /****************************
  * ANNYANG TEST
  ****************************/


  if (annyang) {

  alert("annyang activated");

  annyang.setLanguage('fr-FR');

  // Let's define our first command. First the text we expect, and then the function it should call
  var commands = {
    
    'alerte *val': function(val) {
      telexWrite("commande vocale détéctée");
      geocodeAnnyang(val);

    }

  };

  // Add our commands to annyang
  annyang.addCommands(commands);

  // Start listening. You can call this here, or attach this call to an event, button, etc.
  annyang.start();
}



  /*************************
  *    SOCKET.IO
  *************************/

  socket.on('alert', function(message) {
   telexWrite("Alerte : " + message.address);

 });

  socket.on('rescuer_position', function(coords) {
   
   var lat = coords.lat;
   var lng = coords.lng;

   var marker_rescuer = new google.maps.Marker({
    position: new google.maps.LatLng(lat, lng),
    map: map,
    title:"Secouriste"
  });


   socket.on('update_post_to_sam', function(obj){
    //telexWrite("update");
    marker_rescuer.setPosition(new google.maps.LatLng(obj.lat, obj.lng));

   });


   marker_rescuer.setIcon('http://maps.google.com/mapfiles/ms/icons/purple-dot.png');
   marker_rescuer.setMap(map);
 });

  //Inform server that this is a SAMU CLIENT 
  socket.emit('connection_id', 'SAMU');


  //Welcome Message
  telexWrite("Bienvenue sur GéoSAVE !");

  var search_field = document.getElementById("search");


  //Options:
  var mapOptions = {
    center: {
      lat: 48.856638,
      lng: 2.3100
    },
    zoom: 13,
    disableDefaultUI: true,
    styles: map_style_default
  };

  //Creating the map
  map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);


  /*------------------------------------------------------------
                        ACTION LISTENERS
  -------------------------------------------------------------*/


  google.maps.event.addListener(map, "click", function(event) {

    //reverseGeocoding(event.latLng.lat(), event.latLng.lng());

  });

  google.maps.event.addListener(pin, 'dragend', function() 
  {
    bufferLat = pin.getPosition().lat();
    bufferLng = pin.getPosition().lng();
    reverseGeocoding(pin.getPosition().lat(), pin.getPosition().lng());
  });


  google.maps.event.addListener(map, "rightclick", function(event) {

    var lat = event.latLng.lat();
    var lng = event.latLng.lng();
    
    triggerAlert(lat, lng);


});

search_field.addEventListener("keyup", function(event) {


  if(search_field.value.length > 1)
  Geocoding(search_field.value, event.keyCode);
  else{

    for (var i = 1; i < 4; i++) {
          document.getElementById("res" + i).innerHTML = "";
    }

  }

});







document.getElementById("res1").addEventListener("click", function(){

  preAlert(selectedLat[0], selectedLng[0], selectedAdress[0]);
});

document.getElementById("res2").addEventListener("click", function(){
  preAlert(selectedLat[1], selectedLng[1], selectedAdress[1]);
});

document.getElementById("res3").addEventListener("click", function(){
  preAlert(selectedLat[2], selectedLng[2], selectedAdress[2]);
});




document.getElementById("res1").addEventListener("mouseenter", function(){
  clearAllItems();
  document.getElementById("res1").style = "background-color: green;";
  selectedItem = 1;
});

document.getElementById("res1").addEventListener("mouseleave", function(){
  document.getElementById("res1").style = "";
  selectedItem = 0;
});


document.getElementById("res2").addEventListener("mouseenter", function(){
  clearAllItems();
  document.getElementById("res2").style = "background-color: green;";
  selectedItem = 2;
});

document.getElementById("res2").addEventListener("mouseleave", function(){
  document.getElementById("res2").style = "";
  selectedItem = 0;
});



document.getElementById("res3").addEventListener("mouseenter", function(){
  clearAllItems();
  document.getElementById("res3").style = "background-color: green;";
  selectedItem = 3;
});

document.getElementById("res3").addEventListener("mouseleave", function(){
  document.getElementById("res3").style = "";
  selectedItem = 0;
});







  //startLoop();
}


function clearAllItems(){

 for (var i = 1; i < 4; i++) {
  document.getElementById("res" + i).style = "";   
}

}

/*---------------------------------------------------------------
          END OF INIT FUNCTION
          --------------------------------------------------------------*/


//Reverse geocoding : From GPS coords to Formatted Adress
function reverseGeocoding(lat, lng) {

  
  var Url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&key=AIzaSyDisErcqKz-yrLzBcKN6zPU4JJkO2Z2Lwg";
  var xmlHttp = null;

  xmlHttp = new XMLHttpRequest();   
  xmlHttp.open("GET", Url, true);
  xmlHttp.send(null);
  xmlHttp.onreadystatechange = function() {
    if (xmlHttp.readyState == 4 && (xmlHttp.status == 200 || xmlHttp.status == 0)) {


      
      var myJSONResult = JSON.parse(xmlHttp.responseText);
      var adresse = myJSONResult.results[0].formatted_address;
      geocoded_adress = adresse;

      var cS = '<div id="content">'+
      '<div id="siteNotice">'+
      '</div>'+
      '<h2 id="firstHeading" class="firstHeading">' +
      adresse+
      '</h2>'+
      '<div id="bodyContent">'+
      '<p>'+
      '<button onclick="javascript: triggerAlert('+ bufferLat + ','+ bufferLng +')">Déclencher l\'alerte</button>'+
      '<button onclick="javascript: removePin()">Annuler</button>'+
      '</p>'+
      '</div>'+
      '</div>';
      pin_infowindow.setOptions({
      content: cS
      });


    }
  }


  


}


//Optimized with the city of PARIS
function Geocoding(address, keyCode) {


  var Url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&components=administrative_area:Paris|country:FR&key=AIzaSyDisErcqKz-yrLzBcKN6zPU4JJkO2Z2Lwg";
  var xhr = null;
  var myJSONResult = null;

  xhr = new XMLHttpRequest();

  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 0)) {

      var myJSONResult = JSON.parse(xhr.responseText);
      if (myJSONResult.status == 'OK') {


        if(keyCode == 8){
          if(document.getElementById("search").value.length < 2){
            for (var i = 1; i < 4; i++) {
              document.getElementById("res" + i).innerHTML = "";
            }

          }

          clearAllItems();
          selectedItem = 0

        }



         if(keyCode == 13){  //Enter
          if(selectedItem==0)
            preAlert(myJSONResult.results[0].geometry.location.lat, myJSONResult.results[0].geometry.location.lng, myJSONResult.results[0].formatted_address);
          else
           preAlert(myJSONResult.results[selectedItem-1].geometry.location.lat, myJSONResult.results[selectedItem-1].geometry.location.lng, myJSONResult.results[selectedItem-1].formatted_address);
           
         }

          if(keyCode == 40){ //  V       
            var next = 1+selectedItem;
            if(selectedItem < 3 &&  document.getElementById("res" + next).innerHTML != ""){

              selectedItem != 0 ? document.getElementById("res" + selectedItem).style = "" : 0;
              selectedItem++;
              document.getElementById("res" + selectedItem).style = "background-color: green;";
            }

          }

          if(keyCode == 38){ //  A


            if(selectedItem != 0){
              document.getElementById("res" + selectedItem).style = "";
              selectedItem--;
              selectedItem != 0 ? document.getElementById("res" + selectedItem).style = "background-color: green;" : 0;
            }

          }




          if(myJSONResult.results[0] != null){
            document.getElementById("res1").innerHTML = myJSONResult.results[0].formatted_address;
            selectedAdress[0] = myJSONResult.results[0].formatted_address;
            selectedLat[0] = myJSONResult.results[0].geometry.location.lat;
            selectedLng[0] = myJSONResult.results[0].geometry.location.lng;
          }
          else
            document.getElementById("res1").innerHTML = "";
          if(myJSONResult.results[1] != null){
            document.getElementById("res2").innerHTML = myJSONResult.results[1].formatted_address;
            selectedAdress[1] = myJSONResult.results[1].formatted_address;
            selectedLat[1] = myJSONResult.results[1].geometry.location.lat;
            selectedLng[1] = myJSONResult.results[1].geometry.location.lng;
          }
          else
            document.getElementById("res2").innerHTML = "";
          if(myJSONResult.results[2] != null){
            document.getElementById("res3").innerHTML = myJSONResult.results[2].formatted_address;
            selectedAdress[2] = myJSONResult.results[2].formatted_address;
            selectedLat[2] = myJSONResult.results[2].geometry.location.lat;
            selectedLng[2] = myJSONResult.results[2].geometry.location.lng;
          }
          else
            document.getElementById("res3").innerHTML = "";


        } else {
          if(myJSONResult.status != "ZERO_RESULTS")
          telexWrite(myJSONResult.status + "\n" + myJSONResult.error_message);
        }

      }
    };


    xhr.open("GET", Url, true);
    xhr.send(null);
  }

  function telexWrite(content) {

    var temp = document.getElementById("telex").innerHTML;
    document.getElementById("telex").innerHTML = content + "<hr>" + temp;

  }

  function displayAllSalvers() {

    url = 'http://localhost/controller.php?action=getAllSalvers';
    http_req = null;
    res = null;
    http_req = new XMLHttpRequest();
    http_req.open("GET", url, true);
    http_req.send(null);
    http_req.onreadystatechange = function() {
      if (http_req.readyState == 4 && (http_req.status == 200 || http_req.status == 0)) {

      //clear markers
      for (var i = 0; i < markers_salvers.length; i++) {
        markers_salvers[i].setMap(null);
      };


      res = JSON.parse(http_req.responseText);
      //alert("Salver ID :" + res[0].SalverID + "lat :" + res[0].lat + "lng :" + res[0].lng);

      console.log(res.length + "\n");
      for (var i = 0; i < res.length; i++) {

        var image = 'http://localhost/View/salver_marker.png';

        var marker = new google.maps.Marker({
          position: new google.maps.LatLng(res[i].lat, res[i].lng),
          title: res[i].SalverID,
          map: map,
          icon: image
        });

        markers_salvers.push(marker);
      }



    }
  }
}



// STARTS and Resets the loop if any
function startLoop() {
  if (myInterval > 0) clearInterval(myInterval); // stop
  myInterval = setInterval("displayAllSalvers()", iFrequency); // run
}

//Dark-Light mode
function changelight(val) {

  if(val == "day"){
    map.setOptions({
      styles: map_style_default
    });
  }


  if(val == "night"){
    map.setOptions({
      styles: map_style_dark
    });
  }

}


function removeAlert(id){



  marker_rescuer.setMap(null);
  

  markerAlert[id].setMap(null);
  cityCircle[id].setMap(null);
  document.getElementById("alertdiv" + id).parentNode.removeChild(document.getElementById("alertdiv" + id));
  //document.getElementById("alertdiv" + count).innerHTML = "";
  count--;

  if(count == 0)
    changelight("day");
  
}



function removePin(){

  pin_infowindow.close();
  pin.setMap(null);

}

function triggerAlert(lat,lng){


    removePin();
    count++;

    //Center the map on the alert
    map.setCenter(new google.maps.LatLng(lat, lng));

    //Set the zoom
    map.setZoom(16);

    //Display circle on the map
    circleOptions[count] = {
      strokeColor: '#FF0000',
      strokeOpacity: 0.4,
      strokeWeight: 2,
      fillColor: '#FFAAAA',
      fillOpacity: 0.35,
      map: map,
      center: new google.maps.LatLng(lat, lng),
      radius: 500
    };
    // Add the circle for this city to the map.
    cityCircle[count] = new google.maps.Circle(circleOptions[count]);

    //Add listener to the circle 
    google.maps.event.addListener(cityCircle[count], "rightclick", function(event) {

      alert("lat: " + event.latLng.lat());

    });







    changelight("night");


    var Url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&key=AIzaSyDisErcqKz-yrLzBcKN6zPU4JJkO2Z2Lwg";
    var xmlHttp = null;

    xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", Url, true);
    xmlHttp.send(null);
    xmlHttp.onreadystatechange = function() {
      if (xmlHttp.readyState == 4 && (xmlHttp.status == 200 || xmlHttp.status == 0)) {

        var myJSONResult = JSON.parse(xmlHttp.responseText);
        var adresse = myJSONResult.results[0].formatted_address;

        //Creating alert object
        var alert = {
          position: {
            lat: lat,
            lng: lng,
          },
          address: adresse
        };



        
        socket.emit('messageFromSAMU', alert);



         //Add alert div to alert container

         document.getElementById('alertcontainer').innerHTML += "<div class='alert' id='alertdiv" + count + "'>Alerte " +  count + "<br>" + adresse.split(",")[0] + "<br><a href='javascript:removeAlert(" + count + ");'>Mettre fin a l'alerte</a></div>";



         var contentString = '<div id="content">'+
         '<div id="siteNotice">'+
         '</div>'+
         '<h3 id="firstHeading" class="firstHeading">Alerte ' + count +'</h3>'+
         '<div id="bodyContent">'+
         '<p>'+
         adresse.split(',')[0] + ' ' + adresse.split(',')[1] +
         '</p>'+
         '<p><a href="https://en.wikipedia.org/w/index.php?title=Uluru&oldid=297882194">'+
         'Fin de l\'alerte</a> '+
         '</p>'+
         '</div>'+
         '</div>';

         var infowindow = new google.maps.InfoWindow({
          content: contentString
        });







         markerAlert[count] = new google.maps.Marker({
           position: new google.maps.LatLng(lat, lng),
           map: map,
           title:"Alerte"
         });

     // infowindow.open(map,marker);




   }
 }


}


function preAlert(lat,lng, address){


bufferLat = lat;
bufferLng = lng;

for (var i = 1; i < 4; i++) {
  document.getElementById("res" + i).innerHTML = "";
} 
clearAllItems();           
map.setCenter(new google.maps.LatLng(lat, lng));
map.setZoom(16);



var contentString = '<div id="content">'+
      '<div id="siteNotice">'+
      '</div>'+
      '<h2 id="firstHeading" class="firstHeading">' +
      address +
      '</h2>'+
      '<div id="bodyContent">'+
      '<p>'+
      '<button onclick="javascript: triggerAlert('+ bufferLat + ','+ bufferLng +')">Déclencher l\'alerte</button>'+
      '<button onclick="javascript: removePin()">Annuler</button>'+
      '</p>'+
      '</div>'+
      '</div>';

  pin_infowindow = new google.maps.InfoWindow({
      content: contentString
  });


pin.setOptions({
  position: new google.maps.LatLng(lat, lng),
  map: map,
  draggable: true,
  title:"Alerte"
})

pin.setMap(map);
pin_infowindow.open(map,pin);



}

function geocodeAnnyang(address){



  var Url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&components=administrative_area:Paris|country:FR&key=AIzaSyDisErcqKz-yrLzBcKN6zPU4JJkO2Z2Lwg";
  var xhr = null;
  var myJSONResult = null;

  xhr = new XMLHttpRequest();

  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4 && (xhr.status == 200 || xhr.status == 0)) {

      var myJSONResult = JSON.parse(xhr.responseText);
      if (myJSONResult.status == 'OK') {

        if(myJSONResult.results[0] != null)
        preAlert(myJSONResult.results[0].geometry.location.lat, myJSONResult.results[0].geometry.location.lng, myJSONResult.results[0].formatted_address);


        } else {
          if(myJSONResult.status != "ZERO_RESULTS")
          telexWrite(myJSONResult.status + "\n" + myJSONResult.error_message);
        }

      }
    };


    xhr.open("GET", Url, true);
    xhr.send(null);
  


}




google.maps.event.addDomListener(window, 'load', initialize);
