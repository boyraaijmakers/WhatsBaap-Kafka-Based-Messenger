

const express = require('express');
const bodyParser = require('body-parser');

const zk = require('node-zookeeper-client');
const ZookeeperWatcher = require('zookeeper-watcher');

var chatroomStatus = "Not Registered";

var zkClient = new ZookeeperWatcher({
    hosts: ['127.0.0.1:2181'],
    root: '/',
});
var id = null;

function handleWatcher(path, successState, value, res) {
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
}

function createRequest(username, action, res) {
    console.log("Doing request");
    var done = false;
    zkClient.create(
        "/request/" + action + "/" + username,
        new Buffer("-1"),
        zk.CreateMode.PERSISTENT,
        (error, path) => {
            if (error) {
                return;
            }

            zkClient.watch(
                path,
                (err, value) => {
                    if(done) return;
                    done = true;
                    chatroomStatus = "Pending";
                    handleWatcher(
                        path,
                        action === "enroll" ? "Registered" : "Not Registered",
                        "1",
                        res
                    );
                    
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
const port = 3001;

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.get('/', (request, response) => {
    response.send('Hello from Express!');
});

app.post("/request/enroll", (req, res) => {
    createRequest(req.body.name, "enroll", res);
});

app.post("/request/quit", (req, res) => {
    var body = req.body;
    createRequest(body["id"], body["username"], "quit", res);
});

app.post("/login", (req, res) => {

});

app.post("/logout", (req, res) => {
    zkClient.disconnect();
});

app.listen(port, (err) => {
    if (err) {
        return console.log('something bad happened', err)
    }

    console.log(`server is listening on ${port}`)
});

zkClient.once("connected", (err) => {
    if(err) {
        console.log(err);
    } else {
        console.log("Connected!");
    }
});