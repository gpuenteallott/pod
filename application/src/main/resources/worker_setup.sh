#!/bin/bash

ln -s /home/ubuntu /home/pod
chown tomcat7 /home/
chown tomcat7 /home/pod

LOG="/home/pod/setup.log"
HOME=/home/pod

echo ''  >> $LOG
echo `date` " - Worker setup started" >> $LOG

#########################################################

# The core public dns will be dynamically added before passing the script to the worker
MANAGER_LOCAL_IP=
# The worker id. Also dynamically set
WORKER_ID=
#The identifier of the POD cloud
NAME=

TERMINATION_TIME=

# The repository URL
REPO_URL=
REPO_NAME=`echo $REPO_URL | cut -d'/' -f 5`

# The key pair and security group assigned
KEYPAIR=
SECURITY_GROUP=

EC2_INSTANCE_ID=$(ec2metadata --instance-id)
LOCAL_IP=$(ec2metadata --local-ipv4)
PUBLIC_IP=$(ec2metadata --public-ipv4)

##########################################################

echo "name=$NAME" >> $HOME/server.properties
echo "managerLocalIp=$MANAGER_LOCAL_IP" >> $HOME/server.properties
echo "workerId=$WORKER_ID" >> $HOME/server.properties
echo "role=worker" >> $HOME/server.properties
echo "securityGroup=$SECURITY_GROUP" >> $HOME/server.properties
echo "keypair=$KEYPAIR" >> $HOME/server.properties
echo "instanceId=$EC2_INSTANCE_ID" >> $HOME/server.properties
echo "localIp=$LOCAL_IP" >> $HOME/server.properties
echo "publicIp=$PUBLIC_IP" >> $HOME/server.properties
echo "repoURL=$REPO_URL" >> $HOME/server.properties
echo "terminationTime=$TERMINATION_TIME" >> $HOME/server.properties

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

echo `date` " - Getting the project $REPO_NAME" >> $LOG


# Get the project
cd $HOME
wget $REPO_URL/archive/master.zip
unzip master.zip

# Build the project
cd pod-master/application
mvn clean install

mv target/*.war /var/lib/tomcat7/webapps/ROOT.war

mkdir $HOME/app
sudo chown tomcat7 $HOME/app
sudo chgrp tomcat7 $HOME/app
	
sudo service tomcat7 start

echo `date` " - Finishing installation" >> $LOG

# Creating soft links to access easily the server logs from the home folder
ln -s /var/lib/tomcat7/logs $HOME/server_logs

cd $HOME
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