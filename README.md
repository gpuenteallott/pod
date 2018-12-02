# POD - Processing On Demand platform

The purpose of POD is make deployment for application developers nice and easy.

Imagine that you have developed some application that requires some kind of processing (language doesn't matter). The next step would be start planning how to upload your app, manage the servers, scalability constraints... WAIT! Don't do it!

Better than that, check this out:

1. **Download POD's deployment script**
2. **Execute it to create your POD cloud**
3. **Write a small installation script that downloads your app code**
4. **Use the command line tools to create a new activity, passing the script you just created**
5. **Voilà! Start new executions of your app code whenever you want**

Don't worry about configuring the whole application server side of your app.

POD will also take care of:

* Managing communication between servers
* Recovering automatically from server failures
* Launching more servers if traffic grows
* Shutting down servers if traffic decreases

As you know, servers cost money. POD is a cost effective plafform because it will give you the ability to spend only for what you are using, and you can always establish policies to manage its behaviour.

Project progress board: https://trello.com/b/8YPqpjn4/pod

# Architecture diagram

![Architecture Diagram](https://user-images.githubusercontent.com/1557348/49338395-e370b180-f620-11e8-8fd5-ca4bd6742189.png "Architecture Diagram")

# Interface Documentation

Messages from User to Manager.

## `newActivity`

Creates a new activity and installs it in one worker to validate it. To check the result of the validation, a getActivityStatus request must be sent.
The installation script must download the code for the activity and compile it if necessary. An executable file called `main.sh` must be in the root of the activity directory

Parameters:

* `name`, a unique string for the activity. If it exists, an error will be returned
* `installationScriptLocation`, a URL containing the script with the installation commands

Example:
```
curl 'http://localhost:8080/POD/?action=newActivity&installationScriptLocation=https://www.dropbox.com/s/zcv11g458ks2g99/install.sh?dl=1&name=a' | python -mjson.tool
```

## `getActivityStatus`

Retrieves the activity status, informing about if it is rejected, being verified, uninstalling or approved. Also, an array including all workers where it is installed.

Parameters:

* `name`, the identifier of the activity

Example:
```
curl 'http://localhost:8080/POD/?action=getActivityStatus&name=a' | python -mjson.tool
```

## `deleteActivity`

Deletes the activity given by its name. The deletion process starts by sending an uninstall notification to all workers. When the activity is uninstalled from all workers, it’s records will be removed from the system.

Parameters:

* `name`, the identifier of the activity

Example:
```
curl 'http://localhost:8080/POD/?action=deleteActivity&name=a' | python -mjson.tool
```

## `newExecution`

Start a new execution of the given activity. The execution will be queued if no workers can start it directly. To check the status of the execution use `getExecutionStatus`.
The response will contain an executionId that will be necessary to check the status.

Parameters:

* `name`, the identifier of the activity
* `input`, optional, it will be passed to the main.sh script as the first argument $1

Example:
```
curl 'http://localhost:8080/POD/?action=newExecution&name=a' | python -mjson.tool
```

## `getExecutionStatus`

The current status of the execution will be retrieved, including the standard output and standard error that the execution has produced so far. In case the execution is finished, all it’s output will be retrieved and all record for this execution will be deleted from the system.

Parameters:

* `executionId`, unique id that identifies an execution

Example:
```
curl 'http://localhost:8080/POD/?action=getExecutionStatus&executionId=3' | python -mjson.tool
```

## `terminateExecution`

Terminate an execution in case it isn’t finished. The execution will be stopped and marked as terminated.

Parameters:

* `executionId`, unique id that identifies an execution

Example:
```
curl 'http://localhost:8080/POD/?action=terminateExecution&executionId=3' | python -mjson.tool
```
