

const express = require('express');
const bodyParser = require('body-parser');

const zk = require('node-zookeeper-client');
const ZookeeperWatcher = require('zookeeper-watcher');

var chatroomStatus = "Not Registered";
var sessions = [];

function registerSession () {
    var zkClient = new ZookeeperWatcher({
        hosts: ['127.0.0.1:2181'],
        root: '/',
    });
    var id = null;

    return zkClient.once("connected", (err) => {
        if(err) {
            console.log(err);
        } else {
            id = sessions.push(zkClient);
            console.log("New connection! Giving it ID " + id);
            return id;
        }
    });
}

function handleWatcher(path, successState, value, res) {
    if (value === "0") {
        console.log("Error in event!");
    } else if (value === "1" || value === "2") {
        console.log("Success on event!");
        chatroomStatus = successState;
        zkClient.remove(
            path,
            -1,
            (error) => {
                if (error) {
                    console.log(error.stack);
                    return;
                }

                console.log("Node %s succesfully deleted", path);
                res.send("Success!");
            }
        );
    } else {
        console.log("Unknown response from Manager.");
    }
}

function createRequest(id, username, action, res) {
    session[id].create(
        "/request/" + action + "/" + req.body["username"],
        new Buffer("-1"),
        zk.CreateMode.PERSISTENT,
        (error, path) => {
            if (error) {
                res.send(error.stack);
                return;
            }

            sessions[id].watch(
                path,
                (err, value) => {
                    if (value.toString() !== "-1") {
                        chatroomStatus = "Pending";
                        handleWatcher(
                            path,
                            action === "enroll" ? "Registered" : "Not Registered",
                            value.toString(),
                            res
                        );
                    }
                }
            );
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
})

app.post("/request/enroll", (req, res) => {
    var body = req.body;
    createRequest(body["id"], body["username"], "enroll", res);
});

app.post("/request/quit", (req, res) => {
    var body = req.body;
    createRequest(body["id"], body["username"], "quit", res);
});

app.post("/login", (req, res) => {

});

app.post("/logout", (req, res) => {
    sessions[req.body["id"]].disconnect();
    sessions.remove(req.body["id"]);
    console.log(sessions);
});

app.listen(port, (err) => {
    if (err) {
        return console.log('something bad happened', err)
    }

    console.log(`server is listening on ${port}`)
});