# Running Wildfly in Eclipse

The most important part to start the application in a server controlled by
Eclipse correctly, is the Deployment Assembly from the project properties. Here
you have to make shure, that all maven dependencies and references projects are
added:

![Deployment Assembly in Eclipse](images/deployment-assembly-properties.png)

Otherwise, you will get class not found exceptions in the runtime.
