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