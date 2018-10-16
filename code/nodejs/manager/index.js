

const express = require('express');
const bodyParser = require('body-parser');

const zk = require('node-zookeeper-client');
const ZookeeperWatcher = require('zookeeper-watcher');

function createZkTreeStructure () {

    structure = ["/request", 
            "/request/enroll", 
            "/request/quit", 
            "/registry",
            "/online"];

    for (var i in structure) {
        zkClient.create(structure[i], (err) => {
            if (err) return false;
        });
    }

    console.log("Initial structure created!");

    return true;
}

function setWatchers() {
    watchPaths = ["/request/enroll",
                  "/request/quit",
                  "/online"];

    for(var i in watchPaths) {
        watcher(watchPaths[i]);
        console.log("Now watching children of " + watchPaths[i]);
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
                for (var i in children)
                handleWatcherResult(path, children[i]);
            }
        }
    );
}

function handleWatcherResult(path, child) {
    if (path == "/request/enroll") {
        registerUser(child);
    } else if (path == "/request/quit") {
        removeUser(child);
    } else if (path == "/online") {
        loginUser(child)
    }
}

function loginUser(user) {
    zkClient.exists('/registry/' + user, (err, stat) => {
        if(stat) {
            if(!userHasKafkaTopic(user)) {
                createKafkaTopic(user);
            } 
        } else{
            console.log("Attempt of unregistered user " + user + " to log in!");
        }
    });
}

function userHasKafkaTopic(user) {
    return false;
}

function createKafkaTopic(user) {

}

function deleteKafkaTopic(user) {

}

function registerUser(user) {
    zkClient.exists('/registry/' + user, (err, stat) => {
        newState = (err) ? 0 : (stat) ? 2 : 1; 

        if(newState == 1) {
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
                    console.log(err.stack);
                    return;
                }
            }
        );

        console.log("Handled registraton for " + user + " with new state " + newState + "!");
    });
}

function removeUser(user) {
    zkClient.exists('/registry/' + user, (err, stat) => {
        newState = (err) ? 0 : (stat) ? 1 : 2; 

        if(newState == 1) {
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
                    console.log(err.stack);
                    return;
                }
            }
        );

        if(newState == 1) {
            deleteKafkaTopic(user);
        }

        console.log("Handled deletion for " + user + " with new state " + newState + "!");
    });
}

function getRegisteredUsers(res) {
    zkClient.getChildren(
        "/registry",
        (error) => {
            console.log('Got watcher event: %s', event);
        },
        (error, children, stat) => {
            res.send(children);
        }
    );
}

/*
* Define the Express API
*
**/

const app = express();
const port = 3000;

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());



app.get('/', (request, response) => {
    response.send('Hello from Express!');
});

app.post('/create', (req, res) => {
    res.send(registerSession().toString());
});

app.post('/init', (req, res) => {
    if(createZkTreeStructure()) {
        result = true;
        setWatchers();
    } else {
        result = false;
    }

    res.send(result);
});

app.post('/onlineUsers', (req, res) => {
    getRegisteredUsers(res);
});



app.listen(port, (err) => {
    if (err) {
        return console.log('something bad happened', err)
    }

    console.log(`server is listening on ${port}`)
});

var zkClient = new ZookeeperWatcher({
    hosts: ['127.0.0.1:2181'],
    root: '/',
});
var id = null;

return zkClient.once("connected", (err) => {
    if(err) {
        console.log(err);
    } else {
        console.log("Connected!")
    }
});