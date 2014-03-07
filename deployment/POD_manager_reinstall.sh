#!/bin/bash

################################################
# POD - Processing On Demand open source platform
#
# Installation script of manager in a server
# http://github.com/gpuenteallott/pod
#
# POD_manager_reinstall.sh
# This script pulls the code from the GitHub repo and redeploys it in the server
#
# Usage:
#
#      ssh user@host "bash -s" < POD_manager_reinstall.sh ["db"]
#
#      -db for reseting the database
#
################################################

LOG="/home/pod/setup.log"

echo "" >> $LOG
echo `date` " - Server redeploy" >> $LOG
echo `date` " - Server redeploy"
echo `date` " - Saving properties" >> $LOG
echo `date` " - Saving properties"

# Remove the default Tomcat Webapp
sudo service tomcat7 stop > /dev/null
sudo mv /var/lib/tomcat7/webapps/ROOT/WEB-INF/classes/AwsCredentials.properties ~/AwsCredentials.properties.tmp # Save credentials file

echo `date` " - Removing previous version" >> $LOG
echo `date` " - Removing previous version"
sudo rm -R /var/lib/tomcat7/webapps/ROOT.war
sudo rm -R /var/lib/tomcat7/webapps/ROOT
sudo service tomcat7 start > /dev/null

if [ -e ~/master.zip ]; then 
	rm ~/master.zip
fi
if [ -e ~/pod-master ]; then 
	rm -R ~/pod-master
fi
if [ -e ~/pod.sql ]; then 
	rm ~/pod.sql
fi

echo `date` " - Getting the project" >> $LOG
echo `date` " - Getting the project"

# Get the project
cd ~
wget https://github.com/gpuenteallott/pod/archive/master.zip > /dev/null 2>&1
unzip master.zip  > /dev/null

echo `date` " - Building project" >> $LOG
echo `date` " - Building project"

# Build the project
cd pod-master/application
mvn clean install > /dev/null

sudo mv target/*.war /var/lib/tomcat7/webapps/ROOT.war


# Import database schema
cd ~
mv pod-master/application/database/*.sql pod.sql

if [ $# == 1 ] && [ $1 == 'db' ]; then
	echo `date` " - Importing database" >> $LOG
	echo `date` " - Importing database"
	mysql -u root --password=kaerus_123 -h localhost < pod.sql
	rm -R ~/app
fi


cd ~
if [ ! -d ~/app ]; then
	mkdir ~/app
	sudo chgrp tomcat7 ~/app
fi

echo `date` " - Waiting for deployment" >> $LOG
echo `date` " - Waiting for deployment"

# Give time to Tomcat to deploy the app in a folder
sleep 12

echo `date` " - Restoring credentials and properties file" >> $LOG
echo `date` " - Restoring credentials and properties file"

# Restore credentials and name file
sudo mv ~/AwsCredentials.properties.tmp /var/lib/tomcat7/webapps/ROOT/WEB-INF/classes/AwsCredentials.properties

# Clean up
rm ~/master.zip
rm -R ~/pod-master
rm ~/pod.sql

echo `date` " - Server redeployed" >> $LOG
echo `date` " - Server redeployed"

# Exit the ssh
exit