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
#       Save your Access Key and Secret Key by:
#       $ aws config
#
# Usage: 
#        ./deployPODinAWS.sh  security_group_name  key_pair_name
#
#####################################################################

echo ""
echo "POD Command Line Interface"

INSTANCE_TYPE=t1.micro
AMI=ami-0b9c9f62 # http://cloud-images.ubuntu.com/locator/ec2/
INSTANCE_SETUP=POD_manager_install.sh

# Check number of arguments
if [[ $# < 2 ]]; then
	echo 'Usage:'
	echo '       ./deployPODinAWS.sh  security_group_name  key_pair_name'
	exit 1
fi

NAME="POD"
SECURITY_GROUP="$1"
KEY_PAIR="$2"

ACCESS_KEY=`grep -i 'aws_access_key_id' ~/.aws/config  | cut -f2 -d'=' | tr -d ' '`
SECRET_KEY=`grep -i 'aws_secret_access_key' ~/.aws/config  | cut -f2 -d'=' | tr -d ' '`
REGION=`grep -i 'region' ~/.aws/config  | cut -f2 -d'='`

# Check for the access and secret key
if [ -z $ACCESS_KEY ] || [ -z $SECRET_KEY ]; then
	echo 'Access key and Secret key not found'
	echo 'Run the configure command for the AWS CLI'
	echo '     $ aws configure'
	echo 'It will create a file ~/.aws/config with the properties'
	exit 1
fi

# Verify that keypair exists
KEYPAIR_EXISTS=`aws ec2 describe-key-pairs --key-names $KEY_PAIR | grep "KeyPairs"`
if [[ $KEYPAIR_EXISTS == '' ]]; then
	echo "Key Pair called $KEY_PAIR doesn't exist"
	echo "Please go to AWS console and create one or use the AWS CLI to do it:"
	echo "$ aws ec2 create-key-pair --key-name MyKeyPair"
fi

# Verify that security group exists
SECURITY_GROUP_EXISTS=`aws ec2 describe-security-groups --group-names $SECURITY_GROUP | grep "SecurityGroups"`
if [[ $SECURITY_GROUP_EXISTS == '' ]]; then
	echo "Security Group called $SECURITY_GROUP doesn't exist"
	echo "Please go to AWS console and create one or use the AWS CLI to do it:"
	echo "$ aws ec2 create-security-group --group-name MySecurityGroup --description 'My security group'"
	echo "And don't forget to open the ports 22 for SSH and 80 for HTTP"
fi

# Modify the install.sh script to include the AWS security credentials for the servers
# We will pass the EC2 instance the modified script file which includes the files
# We sent the secret slashes encoded with the char sequence ########
# Otherwise, the sed command has problems because slashes are part of its syntax
echo ""
cp -f $INSTANCE_SETUP $INSTANCE_SETUP.tmp
sed -i "s/ACCESS_KEY=/ACCESS_KEY=$ACCESS_KEY/g" $INSTANCE_SETUP.tmp
SECRET_SCAPED=`echo $SECRET_KEY | sed -e 's/[\/&]/########/g'`
sed -i "s/SECRET_KEY=/SECRET_KEY=$SECRET_SCAPED/g" $INSTANCE_SETUP.tmp

# Include other flags
RANDOM_TAG_VALUE=$RANDOM
sed -i "s/RANDOM_TAG_VALUE=/RANDOM_TAG_VALUE=$RANDOM_TAG_VALUE/g" $INSTANCE_SETUP.tmp
sed -i "s/CLOUD=/CLOUD=aws/g" $INSTANCE_SETUP.tmp
sed -i "s/KEYPAIR=/KEYPAIR=$KEY_PAIR/g" $INSTANCE_SETUP.tmp
sed -i "s/SECURITY_GROUP=/SECURITY_GROUP=$SECURITY_GROUP/g" $INSTANCE_SETUP.tmp
sed -i "s/AMI=/AMI=$AMI/g" $INSTANCE_SETUP.tmp
sed -i "s/INSTANCE_TYPE=/INSTANCE_TYPE=$INSTANCE_TYPE/g" $INSTANCE_SETUP.tmp

# Run
aws ec2 run-instances --image-id $AMI --count 1 --instance-type $INSTANCE_TYPE --user-data file://$INSTANCE_SETUP.tmp --key-name $KEY_PAIR --security-groups $SECURITY_GROUP --output text | awk '{print $8}' | grep -E -o i-[0-9a-zA-Z]* > instance_ids.tmp

# Change name to the instances
while read line
do
	printf "EC2: AMI $line. Tagged: " && aws ec2 create-tags --resources $line --tags Key=Name,Value=$NAME-manager Key=RandomId,Value=$RANDOM_TAG_VALUE --output text
	INSTANCE_ID=$line
	line=
done < instance_ids.tmp

# Remove aws credentials file
rm $INSTANCE_SETUP.tmp
rm instance_ids.tmp

echo `date` "Done launching. Expect the server to be ready in 10 minutes"
echo ""
echo "Waiting for Manager DNS..."
echo -n "*"
sleep 5

# Retrieve information from the instance launched
#DESCRIPTION=`aws ec2 describe-instances --output=json --instance-ids $INSTANCE_ID`

#echo "Trying to get manager DNS:"
#echo "$DESCRIPTION" | grep -i 'PublicDnsName' | cut -f4 -d\"

MANAGER_DNS=`aws ec2 describe-instances --output=json --instance-ids $INSTANCE_ID | grep -i 'PublicDnsName' | cut -f4 -d\"`

while [ -z "$MANAGER_DNS" ]
do
	echo -n "*"
	sleep 7
MANAGER_DNS=`aws ec2 describe-instances --output=json --instance-ids $INSTANCE_ID | grep -i 'PublicDnsName' | cut -f4 -d\"`
	#echo $MANAGER_DNS
done
echo ""

echo "Manager DNS obtained successfully"
echo "For CLI:"
echo "export POD=\"$MANAGER_DNS\""
echo ""