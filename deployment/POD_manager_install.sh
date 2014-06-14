#!/bin/bash

################################################
# POD - Processing On Demand open source platform
#
# Installation script of manager in a server
# http://github.com/gpuenteallott/pod
#
# POD_manager_install.sh
# This script launches a server to deploy the POD Manager in AWS
#
################################################

ln -s /home/ubuntu /home/pod
chown tomcat7 /home/

LOG="/home/pod/setup.log"
touch $LOG
chown ubuntu $LOG
echo `date` " - Server launched" >> $LOG

# Name for the resources
NAME="POD"

# This are the AWS credentials for the server
# They will be dinamically added from this file by installenv.sh
ACCESS_KEY=
SECRET_KEY=

# This is the flag that indicates this is running in a cloud, contains the cloud name (aws)
CLOUD=

# This flag is used to identify the manager when retrieving its information from the cloud provider
RANDOM_TAG_VALUE=

# This flag is used to identify the keypair name, so in the future all workers will be launched using it
KEYPAIR=

echo `date` " - Updating dependencies" >> $LOG >> $LOG

sudo apt-get -y update

echo `date` " - Installing Java" >> $LOG

sudo apt-get -y install openjdk-7-jre openjdk-7-jdk

# Make sure Java 7 is being used
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
sudo update-java-alternatives -s java-1.7.0-openjdk-amd64

echo `date` " - Installing Tomtat, Maven, Unzip and dos2unix" >> $LOG

sudo apt-get -y install tomcat7 maven2 unzip dos2unix

echo `date` " - Installing MySQL" >> $LOG

# Install mysql avoiding the password prompt message
sudo debconf-set-selections <<< 'mysql-server-5.1 mysql-server/root_password password kaerus_123'
sudo debconf-set-selections <<< 'mysql-server-5.1 mysql-server/root_password_again password kaerus_123'
sudo apt-get -y install mysql-server

echo `date` " - Configuring Tomcat" >> $LOG

# Remove the default Tomcat Webapp
sudo service tomcat7 stop
sudo rm -R /var/lib/tomcat7/webapps/ROOT

# Make Tomcat listen in port 80
sudo sed -i "s/#AUTHBIND=no/AUTHBIND=yes/g" /etc/default/tomcat7
sudo sed -i "s/port=\"8080\"/port=\"80\"/g" /var/lib/tomcat7/conf/server.xml

# Make sure Java 7 is being used
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
sudo update-java-alternatives -s java-1.7.0-openjdk-amd64

echo `date` " - Setting up project" >> $LOG

# Get the project
cd ~
wget https://github.com/gpuenteallott/pod/archive/master.zip
unzip master.zip

# Build the project
cd pod-master/application
mvn clean install

mv target/*.war /var/lib/tomcat7/webapps/ROOT.war

mkdir ~/app
sudo chgrp tomcat7 ~/app

# Retrieve the IP addresses of the server and other information useful inside the cloud
if [ -n $CLOUD ]; then
	echo `date` " - Retrieving information from cloud provider" >> $LOG
	DESCRIPTION=`aws ec2 describe-instances --output=json --filter Key=tag-value,Value=$RANDOM_TAG_VALUE`
	PUBLIC_DNS=`echo "$DESCRIPTION" | grep -i 'PublicDnsName' | cut -f4 -d\"`
	PRIVATE_DNS=`echo "$DESCRIPTION" | grep -i 'PrivateDnsName' | cut -f4 -d\"`
	INSTANCE_ID=`echo "$DESCRIPTION" | grep -i 'InstanceId' | cut -f4 -d\"`
fi

sudo service tomcat7 start

echo `date` " - Importing database" >> $LOG

# Import database schema
cd ~
mv pod-master/application/database/*.sql pod.sql
mysql -u root --password=kaerus_123 -h localhost < pod.sql

echo `date` " - Setting up properties" >> $LOG

# Give time to Tomcat to deploy the app in a folder
sleep 12

# Setup aws credentials file
# The string "########" is used to avoid problems with slashes while using 'sed'
cd /var/lib/tomcat7/webapps/ROOT/WEB-INF/classes
echo "accessKey=$ACCESS_KEY" > AwsCredentials.properties
echo "secretKey=$SECRET_KEY" >> AwsCredentials.properties
# The secret came with slashes converted into ######## to avoid problems
sed -i "s/########/\//g" AwsCredentials.properties
chown tomcat7 AwsCredentials.properties
chgrp tomcat7 AwsCredentials.properties
chmod 600 AwsCredentials.properties


echo `date` " - Finishing installation" >> $LOG

# Creating soft links to access easily the server logs from the home folder
ln -s /var/lib/tomcat7/logs ~/server_logs

cd ~
#rm master.zip
#rm -R pod-master
#rm pod.sql

echo `date` " - Server deployed" >> $LOG