#!/bin/bash

ln -s /home/ubuntu /home/pod

LOG="/home/pod/setup.log"

echo ''  >> $LOG
echo `date` " - Worker setup started" >> $LOG

#########################################################

# The core public dns will be dynamically added before passing the script to the worker
MANAGER_PUBLIC_DNS=
# The worker id. Also dynamically set
WORKER_ID=
#The identifier of the POD cloud
NAME=

echo "Core public DNS: $MANAGER_PUBLIC_DNS" >> $LOG

# The repository URL
REPO_URL=
REPO_NAME=`echo $REPO_URL | cut -d'/' -f 5`

# The key pair and security group assigned
KEYPAIR=
SECURITY_GROUP=

echo "Repo URL: $REPO_URL" >> $LOG

##########################################################

echo "name=$NAME" >> /home/pod/server.properties
echo "managerDns=$MANAGER_PUBLIC_DNS" >> /home/pod/server.properties
echo "workerId=$WORKER_ID" >> /home/pod/server.properties
echo "role=worker" >> /home/pod/server.properties
echo "securityGroup=$SECURITY_GROUP" >> /home/pod/server.properties
echo "keypair=$KEYPAIR" >> /home/pod/server.properties

##########################################################

echo `date` " - Updating dependencies" >> $LOG

sudo apt-get -y update

echo `date` " - Installing Java" >> $LOG

sudo apt-get -y install openjdk-7-jre openjdk-7-jdk 
	
# Make sure Java 7 is being used
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
sudo update-java-alternatives -s java-1.7.0-openjdk-amd64

echo `date` " - Installing Tomtat, Maven, Unzip and dos2unix" >> $LOG

sudo apt-get -y install tomcat7 maven2 unzip dos2unix

# Make sure Java 7 is being used
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
sudo update-java-alternatives -s java-1.7.0-openjdk-amd64

echo `date` " - Getting the project $REPO_NAME" >> $LOG


# Get the project
cd ~
wget $REPO_URL/archive/master.zip
unzip master.zip

# Build the project
cd pod-master/application
mvn clean install

mv target/*.war /var/lib/tomcat7/webapps/ROOT.war

mkdir ~/app
sudo chgrp tomcat7 ~/app
	
sudo service tomcat7 start

echo `date` " - Finishing installation" >> $LOG

# Creating soft links to access easily the server logs from the home folder
ln -s /var/lib/tomcat7/logs ~/server_logs

cd ~
#rm master.zip
#rm -R pod-master
#rm pod.sql

echo `date` " - Worker deployed" >> $LOG

#echo "Building project" >> $LOG
#
# Build the project
#cd $REPO_NAME
#ant
#
# Clean up
#rm /home/pod/master.zip
#
# Set up log files
#touch /home/pod/java_output.txt
#touch /home/pod/java_error_output.txt
#chmod 644 /home/pod/java_output.txt
#chmod 644 /home/pod/java_error_output.txt
#
#echo "Starting execution" >> $LOG
#
# Start execution
#cd /home/pod/$REPO_NAME/bin
#java com.kaerusproject.worker.CoreCommunicationAgent 2> /home/pod/java_error_output.txt > /home/pod/java_output.txt &
#
#echo "Configuring system to start the worker execution during startup" >> $LOG
#
# Set up the execution for every next start of the system
#touch /etc/init.d/kaerus.sh
#echo '#!/bin/bash' > /etc/init.d/kaerus.sh
#echo "cd /home/pod/$REPO_NAME/bin" >> /etc/init.d/kaerus.sh
#echo 'java com.kaerusproject.worker.CoreCommunicationAgent 2> /home/pod/java_error_output.txt > /home/pod/java_output.txt &' >> /etc/init.d/kaerus.sh
#
# Add the kaerus script to be executed only in startup
#sudo update-rc.d kaerus.sh start 20 2 3 4 5 .
#sudo chmod +x /etc/init.d/kaerus.sh
#
#echo `date` "Setup finished" >> $LOG