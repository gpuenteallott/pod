POD - Processing On Demand platform
===

Currently under development

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