var express 	= require('express');
var fs 			= require('fs');
var app			= express();
var mysql		= require('mysql'); 
var connection = mysql.createConnection({
	host     : 'localhost',
	user     : 'root',
	password : '**********'
});

connection.connect();



//Handling routes
app.get('/', function(req, res){
	res.setHeader('Content-Type', 'text/html');
	res.sendfile(__dirname + '/index.html');
})

.get('/script.js', function(req, res){
	res.setHeader('Content-Type', 'text/javascript');
	res.sendfile(__dirname + '/script.js');
})

.get('/style.css', function(req, res){
	res.setHeader('Content-Type', 'text/css');
	res.sendfile(__dirname + '/style.css');
})

.get('/logo_colorized.png', function(req, res){
	res.setHeader('Content-Type', 'image/png');
	res.sendfile(__dirname + '/logo_colorized.png');
})

.get('/headerbg.png', function(req, res){
	res.setHeader('Content-Type', 'image/png');
	res.sendfile(__dirname + '/headerbg.png');
})


//--------------------
// CONNECTION PART
//--------------------
.get('/connexion/:pseudo/:password', function(req, res){
	//Get pseudo and password hash
	var pseudo = req.params.pseudo;
	var pw = req.params.password;
	

	console.log('Connexion depuis ANDROID : ' + req.params.pseudo + ' pw : ' + pw);

	connection.query("SELECT * FROM geosave.salver", function(err, rows, fields) {
		var return_value = 0;

		//If there is an error, throw it to caller
		if (err) throw err;

		//Else...
		for (var i = 0; i < rows.length; i++) {
			
			

			if(rows[i].pseudo == pseudo && rows[i].Password == pw){
				res.write("youhou");
			}																
		}
		res.end("end");
	});		
})


.use(function(req, res, next){
	res.setHeader('Content-Type', 'text/html');
	res.end('Page introuvable');
});


//Loading http server
var server = require('http').Server(app);
//Loading Socket.io
var io = require('socket.io').listen(server, { log: false });

//Handling sockets
io.sockets.on('connection', function(socket) {
	
	console.log('Connexion d\'un client');

	socket.on('connection_id', function(message) {
		if (message == 'SAMU')
			console.log('Un Operateur du SAMU vient de se connecter\n');
else{
		if (message == 'Secouriste')
			console.log('Un Secouriste vient de se connecter');
		else {
			console.log('Client non identifié / id :\n' + message);
		}
}
	})




	/************************
	 * Messages from SAMU 
	 ************************/  
	socket.on('messageFromSAMU', function(message) {
		console.log('Message du samu :' + message.address);
		socket.broadcast.emit('alert', message);
	});


	/************************
	 * Messages from Android 
	 ************************/
	socket.on('messageFromSecouriste', function(message) {
		console.log('Message du secouriste :' + message);
	});

	socket.on('android_connection', function(message) {
		if(message == "true") 
			console.log("Disponible!");
	});

	socket.on('androidConnection',function(id){
		console.log('connexion de '+ id);
	});

	socket.on('androidDisconnection',function(id){
		console.log('déconnexion de '+ id);
	});

	socket.on('android_position', function(coords){
		console.log('Position reçue :' + coords.lat + ", " + coords.lng);
		socket.broadcast.emit('rescuer_position', coords);
	});
	socket.on('updatePos', function(obj){
		console.log('UpdatePOS:' + obj.lat + ' ' + obj.lng + ' ' + obj.id);
		socket.broadcast.emit('update_post_to_sam', obj);
	});

});

server.listen(5000);
