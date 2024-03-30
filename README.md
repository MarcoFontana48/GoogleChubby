# Chubby lock service

This project explores the design and implementation of “Chubby”, a distributed
file system, designed by Google, that offers a lock service for loosely-coupled distributed systems

For an in-depth explanation of the project, please refer to the [report](./report_googleChubby.pdf)
## How to use

In order to successfully launch the project and its tests, make sure to have
Docker, Gradle and Git installed.

Clone the project using

```shell
git clone https://github.com/MarcoFontana48/jGoogleChubby.git
```

Once the project is downloaded, use the following command to set up 3 isolated
chubby cells ’local’, ’cell1’, ’cell2’, to connect to:

```shell
docker stack deploy -c ./docker-compose0.yml local;
docker stack deploy -c ./docker-compose1.yml cell1;
docker stack deploy -c ./docker-compose2.yml cell2;
gradle etcd_setup
```

To connect to the cells using a custom gradle task, use the following command
to access the ’local’ cell using an already registered user ’client0’ with password
’password’:
    
```shell
gradle run_client_0-local
```

Other tasks are already present to also connect to different cells using different
clients, check file ’server/build.gradle.kts’

Once done, to delete all the containers previously created, use the command:

```shell
docker stack rm local; docker stack rm cell1; docker stack rm cell2
```

## Usage example
### how messages are formatted
Before showing some usage examples, it’s necessary to explain how the messages
are formatted. Each message type is formatted the same way:

`[date time] username-messageType:(handleType)path`

i.e. a typical request from a client ’client0’ with an open handle on path ’/ls/local/test.txt’, that will be used to ’write’ content in it, is the following:

`[2024-03-13 09:45:12] client0-request:(write)\ls\local\test.txt< read filecontent`

and the response from the server:

`[2024-03-13 09:45:12] chubby-response:(write)\ls\local\test.txt> hello world!`

### examples
The moment a client connects to a cell, a notification from the server, containing a message, will be sent

`[2024-03-13 09:21:43] chubby-notification:\> acquired initial shared lock on root ’\’ node`

This informs the client that an initial handle (read type) on root node has been successfully created.

In order to open a new handle use command ’open’;
i.e. to open an handle on a ’test.txt’ file that will be used to write file content (on a cell named ’local’) the following command may be executed:

open /ls/local/test.txt write

It will now be shown the request that was sent to the server and the response
that it’s sent back to the client:

`[2024-03-13 09:31:30] client0-request:(read)\< open /ls/local/test.txt write`

`[2024-03-13 09:31:31] chubby-response:(write)\ls\local\test.txt> successfully opened node`

This informs the client that the handle was successfully opened and that it now has an handle on the specified path ’/ls/local/test.txt’ of ’write’ type.
The client may now write file content in it:

`write filecontent hello world!`

`[2024-03-13 09:42:48] client0-request:(write)\ls\local\test.txt<write filecontent hello world!`

`[2024-03-13 09:42:48] chubby-response:(write)\ls\local\test.txt>node content updated successfully`

To read the file content, use the read command:

`read filecontent`

`[2024-03-13 09:45:12] client0-request:(write)\ls\local\test.txt<read filecontent`

`[2024-03-13 09:45:12] chubby-response:(write)\ls\local\test.txt>hello world!`

If a command that cannot be executed with current handle is sent to the server, an error response will be received:

`[2024-03-13 09:48:48] client0-request:(write)\ls\local\test.txt< write acl read new read acl name`

`[2024-03-13 09:48:48] chubby-error:\ls\local\test.txt> it’s not possible to change acl names of current node with ’WRITE’ handle type, acquire ’CHANGE ACL’ handle type before proceeding`

Once done, close the handle using ’close’ command, that automatically unlocks
the node and creates a new handle (read type) on root node.

About event subscriptions, if ’client0’ opens a directory and activates the
child node added subscription, those messages are going to be exchanged:

`[2024-03-13 10:01:36] client0-request:(read)\< open \ls\local\test dir change acl child node added`

`[2024-03-13 10:01:36] chubby-response:(change acl)\ls\local\test dir> successfully opened node`

If ’client1’ now creates a child node of ’test dir’ on the same cell:

`[2024-03-13 10:02:03] client1-request:(read)\< open \ls\local\test dir\test child dir change acl`

`[2024-03-13 10:02:04] chubby-response:(change acl)\ls\local\test dir\test child dir> successfully opened node`

’client0’ will receive the following notification:

`[2024-03-13 10:02:03] chubby-notification:\ls\local\test dir> number of child nodes from currently held node has increased`

That informs the client that the number of child nodes has increased, and the directory from where the event took place: ’\ls\local\test dir’.

Many other examples are provided from the tests that were made, please referto test classes for usage examples; also, once connected to a cell, the command’help’ may be executed for a detailed explanation of the possible commands that may be run.