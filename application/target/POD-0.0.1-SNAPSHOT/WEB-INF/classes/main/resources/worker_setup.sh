#!/bin/bash

ln -s /home/ubuntu /home/user
ln -s /home/debian /home/user

LOG="/home/user/setup.log"

echo `date` "Setup started" >> $LOG

#########################################################

# The core public dns will be dynamically added before passing the script to the worker
MANAGER_PUBLIC_DNS=
# The worker id. Also dynamically set
WORKER_ID=
#The identifier of the POD cloud
NAME=

echo "Core public DNS: $MANAGER_PUBLIC_DNS ; Worker Id: $WORKER_ID" >> $LOG

# The repository URL
REPO_URL=
REPO_NAME=`echo $REPO_URL | cut -d'/' -f 5`
WORKER_CODE_URL=https://github.com/gpuenteallott/pod

# The key pair and security group assigned
KEYPAIR=
SECUTIRY_GROUP=

##########################################################

echo "name=$NAME" >> /home/user/server.properties
echo "corePublicDNS=$CORE_PUBLIC_DNS" > /home/user/server.properties
echo "workerId=$WORKER_ID" >> /home/user/server.properties
echo "role=worker" >> /home/user/server.properties
echo "securityGroup=$SECURITY_GROUP" >> /home/user/server.properties
echo "keypair=$KEYPAIR" >> /home/user/server.properties

##########################################################

echo "Updating dependencies" >> $LOG

sudo apt-get -y update

echo "Installing Java, Ant and Unzip" >> $LOG

sudo apt-get -y install openjdk-7-jre openjdk-7-jdk unzip 

# Make sure Java 7 is being used
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/jre
sudo update-java-alternatives -s java-1.7.0-openjdk-amd64

echo "Getting the project $REPO_NAME" >> $LOG
#
# Get the project
#cd /home/user
#wget $REPO_URL/archive/master.zip
#unzip master.zip
#rm master.zip
#mv $REPO_NAME-master $REPO_NAME
#
#echo "Getting the worker logic" >> $LOG
#
# Get the worker logic
#wget $WORKER_CODE_URL/archive/master.zip
#unzip master.zip
#rm master.zip
#mv kaerus-worker-master kaerus-worker
#mkdir -p $REPO_NAME/src/com/kaerusproject/worker/
#mv kaerus-worker/src/com/eclipsesource $REPO_NAME/src/com/
#mv kaerus-worker/src/com/kaerusproject/worker/*.java $REPO_NAME/src/com/kaerusproject/worker/
#rm -R kaerus-worker
#
#echo "Building project" >> $LOG
#
# Build the project
#cd $REPO_NAME
#ant
#
# Clean up
#rm /home/user/master.zip
#
# Set up log files
#touch /home/user/java_output.txt
#touch /home/user/java_error_output.txt
#chmod 644 /home/user/java_output.txt
#chmod 644 /home/user/java_error_output.txt
#
#echo "Starting execution" >> $LOG
#
# Start execution
#cd /home/user/$REPO_NAME/bin
#java com.kaerusproject.worker.CoreCommunicationAgent 2> /home/user/java_error_output.txt > /home/user/java_output.txt &
#
#echo "Configuring system to start the worker execution during startup" >> $LOG
#
# Set up the execution for every next start of the system
#touch /etc/init.d/kaerus.sh
#echo '#!/bin/bash' > /etc/init.d/kaerus.sh
#echo "cd /home/user/$REPO_NAME/bin" >> /etc/init.d/kaerus.sh
#echo 'java com.kaerusproject.worker.CoreCommunicationAgent 2> /home/user/java_error_output.txt > /home/user/java_output.txt &' >> /etc/init.d/kaerus.sh
#
# Add the kaerus script to be executed only in startup
#sudo update-rc.d kaerus.sh start 20 2 3 4 5 .
#sudo chmod +x /etc/init.d/kaerus.sh
#
#echo `date` "Setup finished" >> $LOG