

INSTANCE_TYPE=t1.micro
AMI=ami-a73264ce # http://cloud-images.ubuntu.com/locator/ec2/
INSTANCE_SETUP=setup.sh


# Check number of arguments
if [ $# != 3 ] && [ $# != 2 ]; then
	echo 'Usage:'
	echo '       ./deploy.sh [name] AWS_ACCESS_KEY AWS_SECRET KEY'
	echo '       name is the identificator of instances, keypair and security group'
	echo '       if no name is given, POD will be used'
	exit 1
fi

# Export variables
if [ $# == 2 ]; then
	NAME="POD"
	export AWS_ACCESS_KEY_ID=$1
	export AWS_SECRET_ACCESS_KEY=$2
fi

if [ $# == 3 ]; then
	NAME=$1
	export AWS_ACCESS_KEY_ID=$2
	export AWS_SECRET_ACCESS_KEY=$3
fi

# Export variables
export AWS_DEFAULT_REGION=us-east-1

# Create keypair, or use existing one
if [ -e "$NAME-key.priv" ]
then
	echo "EC2: Using existing keypair $NAME-key.priv"
fi
if [ ! -e "$NAME-key.priv" ]
then
	aws ec2 create-key-pair --key-name $NAME-key --query 'KeyMaterial' --output text > $NAME-key.priv
	chmod 600 $NAME-key.priv
	echo "EC2: Keypair $NAME-key.priv created"
fi


# Create security group, or use existing one
# Access to ports 80 and 22 from all IP adresses
printf "EC2: Security group " && aws ec2 create-security-group --group-name $NAME-grp --description "Security group for the instances with identifier $NAME"
printf "EC2: Security group " && aws ec2 authorize-security-group-ingress --group-name $NAME-grp --protocol tcp --port 80 --cidr 0.0.0.0/0
printf "EC2: Security group " && aws ec2 authorize-security-group-ingress --group-name $NAME-grp --protocol tcp --port 22 --cidr 0.0.0.0/0
printf "EC2: Security group " && aws ec2 authorize-security-group-ingress --group-name $NAME-grp --protocol tcp --port 8080 --cidr 0.0.0.0/0


# Modify the install.sh script to include the AWS security credentials for the servers
# We will pass the EC2 instance the modified script file which includes the files
# We sent the secret slashes encoded with the char sequence ########
# Otherwise, the sed command has problems because slashes are part of its syntax
echo "EC2: Modifying install script on the fly to include AWS credentials"
cp -f $INSTANCE_SETUP $INSTANCE_SETUP.tmp
sed -i "s/AWS_ACCESS_KEY_ID=/AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID/g" $INSTANCE_SETUP.tmp
SECRET_SCAPED=`echo $AWS_SECRET_ACCESS_KEY | sed -e 's/[\/&]/########/g'`
sed -i "s/AWS_SECRET_ACCESS_KEY=/AWS_SECRET_ACCESS_KEY=$SECRET_SCAPED/g" $INSTANCE_SETUP.tmp

# Modify install.sh to include the name given by the user
sed -i "s/NAME=/NAME=$NAME/g" $INSTANCE_SETUP.tmp
# Launch instances
# using the selected keypair and security group
# Save the ID of the new instances
echo "EC2: instance started"

aws ec2 run-instances --image-id $AMI --count 1 --instance-type $INSTANCE_TYPE --user-data file://$INSTANCE_SETUP.tmp --key-name $NAME-key --security-groups $NAME-grp --output text | awk '{print $8}' | grep -E -o i-[0-9a-zA-Z]* > instance_ids.tmp

# Change name to the instances

while read line
do
	printf "EC2: " && aws ec2 create-tags --resources $line --tags Key=Name,Value=$NAME-Core
	aws ec2 describe-instance-attribute --instance-id $line --attribute publicDnsName
    line=
done < instance_ids.tmp

# Remove aws credentials file
#rm instance_ids.tmp
rm $INSTANCE_SETUP.tmp
rm instance_ids.tmp

