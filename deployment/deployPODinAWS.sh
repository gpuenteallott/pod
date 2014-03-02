#!/bin/bash

#####################################################################
# POD - Processing On Demand open source platform
#
# Deployment of Manager node
# http://github.com/gpuenteallott/pod
#
# core_deploy.sh
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
#        ./deployPODinAWS.sh name
#        name is the identificator of instances, keypair and security group
#
#####################################################################

INSTANCE_TYPE=t1.micro
AMI=ami-0b9c9f62 # http://cloud-images.ubuntu.com/locator/ec2/
INSTANCE_SETUP=POD_manager_setup.sh

# Check number of arguments
if [ $# < 1 ]; then
	echo 'Usage:'
	echo '       ./deployPODinAWS.sh  name  security_group_name  key_pair_name'
	echo '       name is the identificator of instances, keypair and security group'
	exit 1
fi

# Check for the access and secret key
if [ -z ACCESS_KEY ] || [ -z SECRET_KEY ]; then
	echo 'Access key and Secret key not found in environment'
fi

SECURITY_GROUP= $2
KEY_PAIR= $3

aws ec2 run-instances --image-id $AMI --count 1 --instance-type $INSTANCE_TYPE --user-data file://$INSTANCE_SETUP.tmp --key-name $KEY_PAIR --security-groups $SECURITY_GROUP --output text #| awk '{print $8}' | grep -E -o i-[0-9a-zA-Z]* > instance_ids.tmp


