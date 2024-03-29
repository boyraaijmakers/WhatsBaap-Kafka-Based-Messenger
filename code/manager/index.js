require('events').EventEmitter.prototype._maxListeners = 100;

const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const Pusher = require('pusher');

const ZookeeperWatcher = require('zookeeper-watcher');

const MANUAL_MANAGEMENT = true;

var pendingRequests = {
	"/request/enroll": [],
	"/request/quit": []
};

function managerLog(message) {
	var time = new Date()
	console.log("[" + time.toUTCString() + "]  " + message)
}

function createZkTreeStructure() {

	structure = ["/request",
		"/request/enroll",
		"/request/quit",
		"/registry",
		"/online"
	];

	for (var i in structure) {
		zkClient.create(structure[i], (err) => {
			if (err) return false;
		});
	}

	managerLog("Initial structure created!");

	return true;
}

function setWatchers() {
	watchPaths = ["/request/enroll",
		"/request/quit",
		"/online"
	];

	for (var i in watchPaths) {
		watcher(watchPaths[i]);
		managerLog("Now watching children of " + watchPaths[i]);
	}
}

function watcher(path) {
	zkClient.getChildren(
		path,
		(event) => {
			watcher(path);
		},
		(error, children, stat) => {
			if (children.length) {
				pendingRequests[path] = [];
				for (var i in children) {
					handleWatcherResult(path, children[i], i == children.length - 1);
				}
			}
		}
	);
}

function handleWatcherResult(path, child, last) {
	if (path == "/request/enroll") {
		registerUser(child, last);
	} else if (path == "/request/quit") {
		removeUser(child, last);
	} else if (path == "/online") {
		loginUser(child)
	}
}

function loginUser(user) {
	zkClient.exists('/registry/' + user, (err, stat) => {
		if (stat) {
			managerLog(user + " is now logged in!");
			createKafkaTopic(user);
		} else {
			managerLog("Attempt of unregistered user " + user + " to log in! Removing it now...");
			zkClient.remove(
				"/online/" + user,
				(err) => {
					if (err) newState = 0;
				});
		}
	});
}

function createKafkaTopic(user) {
	zkClient.exists("/brokers/topics/" + user, (err, stat) => {
		newState = (err) ? 0 : (stat) ? 2 : 1;

		if (newState == 1) {
			managerLog("It's " + user + "'s first time here. Let me give him a topic!")
			zkClient.create(
				"/brokers/topics/" + user,
				(err) => {
					if (err) newState = 0;
				}
			);
		}
	});
}

function deleteKafkaTopic(user) {
	zkClient.exists("/brokers/topics/" + user, (err, stat) => {
		newState = (err) ? 0 : (stat) ? 2 : 1;

		if (newState == 2) {
			managerLog("Goodbye " + user + "... Removing the topic.");
			zkClient.remove(
				"/brokers/topics/" + user,
				(err) => {
					if (err) newState = 0;
				}
			);
		}
	});
}

function registerUser(user, last) {
	managerLog("Handle register request for new user " + user + "!");

	zkClient.exists('/registry/' + user, (err, stat) => {
		newState = (err) ? 0 : (stat) ? 2 : 1;

		if (newState == 1) {
			if (MANUAL_MANAGEMENT) {
				pendingRequests["/request/enroll"].push({
					name: user
				});
			} else {
				handleRegister(user, newState);
			}
		}

		if (MANUAL_MANAGEMENT && last) pusher.trigger("lssp-manager-channel", "requests", pendingRequests);
	});
}

function handleRegister(user, state) {
	var newState = state;

	if (newState == 1) {
		zkClient.create(
			"/registry/" + user,
			(err) => {
				if (err) newState = 0;
			}
		);
	}

	zkClient.setData(
		"/request/enroll/" + user,
		new Buffer(newState.toString()),
		-1,
		(err, stat) => {
			if (err) {
				managerLog(err.stack);
				return;
			}
		}
	);

	if (MANUAL_MANAGEMENT) watcher("/request/enroll");
}

function removeUser(user, last) {
	managerLog("Handle quit request for user " + user + "!");

	zkClient.exists('/registry/' + user, (err, stat) => {
		newState = (err) ? 0 : (stat) ? 1 : 2;

		if (newState == 1) {
			if (MANUAL_MANAGEMENT) {
				pendingRequests["/request/quit"].push({
					name: user
				});
			} else {
				handleQuit(user, newState);
			}
		}

		if (MANUAL_MANAGEMENT && last) pusher.trigger("lssp-manager-channel", "requests", pendingRequests);
	});
}

function handleQuit(user, state) {
	var newState = state;

	if (newState == 1) {
		zkClient.remove("/registry/" + user, (err) => {
			if (err) newState = 0;
		});
	}

	zkClient.setData(
		"/request/quit/" + user,
		new Buffer(newState.toString()),
		-1,
		(err, stat) => {
			if (err) {
				managerLog(err.stack);
				managerLog("Deze error");
				return;
			}
		}
	);

	if (newState == 1) deleteKafkaTopic(user);

	if (MANUAL_MANAGEMENT) watcher("/request/quit");
}

function getRegisteredUsers(res) {
	zkClient.getChildren(
		"/registry",
		(error, regUsers, stat) => {
			var response = [];
			regUsers = regUsers.toString().split(",");

			zkClient.getChildren(
				"/online",
				(error, onUsers, stat) => {
					onUsers = onUsers ? onUsers : "";
					onUsers = onUsers.toString().split(",");
					for (var i in regUsers) {
						var child = regUsers[i];

						response.push({
							name: child,
							status: onUsers.includes(child)
						});
					}
					res.status(200);
					res.set("Connection", "close");
					res.send(response);
				}
			);
		}
	);
}


var zkClient = new ZookeeperWatcher({
	hosts: ['127.0.0.1:2181'],
	root: '/',
});

zkClient.once("connected", (err) => {
	if (err) {
		managerLog(err);
	} else {
		managerLog("Connected to Zookeeper!");

		if (createZkTreeStructure()) {
			result = true;
			setWatchers();
		} else {
			result = false;
		}
	}
});

const pusher = new Pusher({
	appId: '636509',
	key: 'b8bc6340bc02272f30ed',
	secret: '967f0176b9dea80c176c',
	cluster: 'eu',
	encrypted: true
});

const app = express();
const port = 3000;

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(cors());

app.route('/')
	.get((req, res) => {
		res.status(200);
		res.set("Connection", "close");
		res.send('Hello from Express!');
	});

app.route('/registeredUsers')
	.get((req, res) => {
		getRegisteredUsers(res);
	});

app.route('/registerUser')
	.post((req, res) => {
		handleRegister(req.body.name, req.body.state);
	});

app.route('/removeUser')
	.post((req, res) => {
		handleRegister(req.body.name, req.body.state);
	});

app.route('/getRequests')
	.post((req, res) => {
		pusher.trigger("lssp-manager-channel", "requests", pendingRequests);
	});

app.listen(port, (err) => {
	if (err) {
		return managerLog('something bad happened', err)
	}
	managerLog(`server is listening on ${port}`)
});