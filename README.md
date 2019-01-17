<p align="center">
<a href="http://upm.es/"><img src="https://github.com/boyraaijmakers/LSSP/blob/master/code/worker/target/classes/assets/logo.png?raw=true" width="600px"></a>
</p>

Welcome to the repository of WhatsBaap messenger. This Apache Zookeeper and Apache Kafka powered system uses a manager-worker environment to exchange messages between users who want to chat. The workers are used by chatting users whereas the manager keeps track of registers, quits and online users. This introduction will guide you through the main set-up of the project.


Universidad Politécnica de Madrid, Spain 🌞


Authors: [@boyraaijmakers](https://github.com/boyraaijmakers) [@philosss](https://github.com/philosss)


## Manager set-up

The manager application is written in NodeJS on top of Express. The manager has two modes:
- Automatic: During automatic management, the manager will directly approve all request for user registers and quits. There is no human intervention in this process.
- Manual: During manual management, a human administrator can manually assess registers and quits of users through the WhatsBaap management portal. Also, a system administrator will be able to view currently registered and online users through the panel.
The management mode of the manager is set in a variable in the code of the main javascript file (`index.js`).

To set up the manager in either modes, one should execute the following steps:
- Make sure the Apache Zookeeper and Apacha Kafka services are running (check with running `jps` in a terminal).
- Make sure the manager is ready for automatic mode by setting the `MANUAL_MANAGEMENT` variable to true for manual and false for automatic management.
- Open a terminal and move to root directory of the manager (the folder with `index.js` and `packages.json`).
- Run the command `npm install` and let the necessary dependencies install.
- Run the manager by executing `node index.js`.
- The manager is now starting.

When in manual management mode, one should also start the management interface. This interface is build using Angular and is run using the following steps:
- Open a terminal and move to the root directory of the interface (the folder with `angular.json` and another `packages.json`).
- Execute the command `npm install`.
- Run the interface by executing `ng serve`.
- Once compilation is done, open up a web browser and go to localhost:4200 to access the interface.

## Worker set-up
The worker application is written in Java using JavaFX for the GUI.
To run the worker it needed that the manager, ZooKeeper and Kafka are running. Consider to edit the file `Config.java` if ports and IP addresses are different than the default one.
To build the project:
- Open the command line in the folder containing the file `pom.xml`
- Type `mvn clean install`
- Execute `mvn exec:java`


<p align="center">
<a href="http://upm.es/"><img src="https://github.com/boyraaijmakers/LSSP/blob/master/code/worker/target/classes/assets/upm.png" width="300px"></a>
</p>
