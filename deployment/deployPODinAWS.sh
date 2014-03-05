#!/bin/bash

#####################################################################
# POD - Processing On Demand open source platform
#
# Deployment of Manager node
# http://github.com/gpuenteallott/pod
#
# deployPODinAWS.sh
# This script launches a server to deploy the POD Manager in AWS
#
# Requires AWS CLI installed
# http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-set-up.html
#
# Previous steps:
#       Export your Access Key and Secret Key by:
#       export ACCESS_KEY <your_access_key>
#	    export SECRET_KEY <your_secret_key>
#
# Usage: 
#        ./deployPODinAWS.sh  security_group_name  key_pair_name
#
#####################################################################

INSTANCE_TYPE=t1.micro
AMI=ami-0b9c9f62 # http://cloud-images.ubuntu.com/locator/ec2/
INSTANCE_SETUP=POD_manager_setup.sh

# Check number of arguments
if [ $# < 1 ]; then
	echo 'Usage:'
	echo '       ./deployPODinAWS.sh  security_group_name  key_pair_name'
	exit 1
fi

# Check for the access and secret key
if [ -z $ACCESS_KEY ] || [ -z $SECRET_KEY ]; then
	echo 'Access key and Secret key not found in environment'
fi

NAME="POD"
SECURITY_GROUP= $1
KEY_PAIR= $2

# Modify the install.sh script to include the AWS security credentials for the servers
# We will pass the EC2 instance the modified script file which includes the files
# We sent the secret slashes encoded with the char sequence ########
# Otherwise, the sed command has problems because slashes are part of its syntax
echo "EC2: Modifying install script on the fly to include AWS credentials"
cp -f $INSTANCE_SETUP $INSTANCE_SETUP.tmp
sed -i "s/ACCESS_KEY=/ACCESS_KEY=$ACCESS_KEY/g" $INSTANCE_SETUP.tmp
SECRET_SCAPED=`echo $SECRET_KEY | sed -e 's/[\/&]/########/g'`
sed -i "s/SECRET_KEY=/SECRET_KEY=$SECRET_SCAPED/g" $INSTANCE_SETUP.tmp

# Run
aws ec2 run-instances --image-id $AMI --count 1 --instance-type $INSTANCE_TYPE --user-data file://$INSTANCE_SETUP.tmp --key-name $KEY_PAIR --security-groups $SECURITY_GROUP --output text | awk '{print $8}' | grep -E -o i-[0-9a-zA-Z]* > instance_ids.tmp

# Change name to the instances

while read line
do
printf "EC2: " && aws ec2 create-tags --resources $line --tags Key=Name,Value=$NAME-Mgr
    line=
done < instance_ids.tmp

# Remove aws credentials file
#rm instance_ids.tmp
rm $INSTANCE_SETUP.tmp
rm instance_ids.tmp