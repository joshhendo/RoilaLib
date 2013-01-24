#PATH=$PATH:/home/user/Desktop/jdk1.7.0_10/bin

SPHINXDL=http://downloads.sourceforge.net/project/cmusphinx/sphinx4/1.0%20beta3/sphinx4-1.0beta3-src.zip?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Fcmusphinx%2Ffiles%2Fsphinx4%2F1.0%2520beta3%2F&ts=1357256121&use_mirror=internode
SPHINXDLNAME=sphinx-src-beta3.zip

JAVADL32=http://download.oracle.com/otn-pub/java/jdk/7u10-b18/jdk-7u10-linux-i586.tar.gz
JAVADL64=http://download.oracle.com/otn-pub/java/jdk/7u10-b18/jdk-7u10-linux-x64.tar.gz
JAVADLNAME=java-sdk.tar.gz

WORKINGDIR=roila-sphinx

SIXDFILE=""
LMFILE=""

#Initial Setup

# Determine processor 32 or 64. This is important for which version of the Java SDK to download
MACHINE_TYPE=32
if [ `uname -m` == 'x86_64' ]; then
  MACHINE_TYPE=64
fi

# We need to go into a fresh working directory. 
if [ -d $WORKINGDIR ]
then
	cd $WORKINGDIR
else
	mkdir $WORKINGDIR
	cd $WORKINGDIR
fi

# Add the JDK that's in this directory to the PATH is needed.
# http://www.ducea.com/2009/03/05/bash-tips-if-e-wildcard-file-check-too-many-arguments/
files=$(ls -d jdk* 2> /dev/null | wc -l)
if [ $files == 1 ]; then
	# The JDK folder exists... add it to the PATH
	ADDTOPATH=:`pwd`/`ls -d jdk*`
	PATH=$PATH$ADDTOPATH
fi

function DownloadJava {
	# Check for Java installed
	# It seems as though Java is included in the SDK, so we don't actually need to worry about whether or not it's installed
	jinst=true;
	command -v java > /dev/null 2>&1 || { echo "Java is needed but it's not installed."; jinst=false; }

	if [ $jinst = false ]; then
		echo ""
	else
		echo ""
		jver=$(java -version)
	fi

	# Check for Java SDK installed
	jsdk=true;
	command -v javac > /dev/null 2>&1 || { echo "Java SDK is needed but it's not installed."; jsdk=false; }

	if [ $jsdk = false ]; then
		echo "No SDK"

		# Proceed to download SDK
		if [ $MACHINE_TYPE == 32 ]; then
			wget -O $JAVADLNAME --no-cookies --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F" $JAVADL32
		else
			wget -O $JAVADLNAME --no-cookies --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F" $JAVADL64
		fi
		
		tar xvfz $JAVADLNAME
	else
		echo "Got SDK"
	fi
}


#TODO See if the user has supplied a custom word list to be compiled, otherwise grab it from roila.org
#TODO Check paramaters to see if there are any files supplied


function DownloadSamples {
	echo "Please input the location of your .6d file, or press ENTER to use default: "
	read SIXDFILE

	if [ $SIXDFILE != "" ]; then
		echo "Please input the location of the corresponding .lm file: "
		read LMFILE
	else
		# Download the default .6d and lm file
		wget https://raw.github.com/joshhendo/RoilaLib/master/files/9990.lm
		wget https://raw.github.com/joshhendo/RoilaLib/master/files/newdict2.6d

		SIXDFILE=`pwd`/newdict2.6d
		LMFILE=`pwd`/9990.lm
	fi

	wget https://raw.github.com/joshhendo/RoilaLib/master/files/roila.config.xml

	# Escape the file properly. see http://stackoverflow.com/questions/471183/linux-command-line-global-search-and-replace
	LMFILE=`echo $LMFILE | sed -e 's/[\/&]/\\\\&/g'`
	SIXDFILE=`echo $SIXDFILE | sed -e 's/[\/&]/\\\\&/g'`

	eval sed -i \'s/D:\\\\java\\\\LEGO_TEST_PC\\\\src\\\\9127.lm/$LMFILE/g\' roila.config.xml
	eval sed -i \'s/D:\\\\java\\\\LEGO_TEST_PC\\\\src\\\\newdict2.6d/$SIXDFILE/g\' roila.config.xml
}

function DownloadJavaLib {
	wget -O RoilaLib.jar https://github.com/joshhendo/RoilaLib/blob/master/compiled/RoilaLib.jar?raw=true
}

function DownloadJavaSource {
	# Download the sample class BTsend.class
	wget -O BTsend.class http://roila.org/wp-content/uploads/2010/04/roila_java.txt

	# Download Sphinx-4 beta 3
	wget -O $SPHINXDLNAME $SPHINXDL
	unzip $SPHINXDLNAME
}

function InstallVoice {
	INSTALL_LOCATION=/usr/share/festival/voices/english
	echo "Please enter an install location, or press enter to use the default (recommended) (default: $ISNTALL_LOCATION):"
	read NEW_INSTALL_LOCATION

	if [ $NEW_INSTALL_LOCATION != "" ]; then
		INSTALL_LOCATION=$NEW_INSTALL_LOCATION
	fi

	MALE=false
	FEMALE=false

	if   [ $1 == '1' ]; then
		MALE=true
		echo "Installing Male Voice"
	elif [ $1 == '2' ]; then
		FEMALE=true
		echo "Installing Female Voice"
	elif [ $1 == '3' ]; then
		MALE=true
		FEMALE=true
		echo "Installing Male and Female Voice"
	fi

	if [ $MALE == true ]
		#Download the voice
		# http://stackoverflow.com/questions/5207974/writing-a-bash-script-that-performs-operations-that-require-root-permissions
		wget -O roila_diphone.tar.gz https://github.com/joshhendo/RoilaLib/blob/master/files/voice/roila_diphone.tar.gz?raw=true
		CURRENT_PATH=`pwd`/roila_diphone
		echo $CURRENT_PATH
		tar -zxvf roila_diphone.tar.gz

		# ensure that the directory exists
		# http://stackoverflow.com/questions/59838/how-to-check-if-a-directory-exists-in-a-shell-script
		if [ -d "$INSTALL_LOCATION" ]; then
			sudo cp -R $CURRENT_PATH $INSTALL_LOCATION
		else
			echo "$INSTALL_LOCATION doesn't exist. Can't install voice until this location exists."
			echo "The voice has been downloaded and extracted locally without been installed."
		fi
	fi

	if [ $FEMALE == true ]
		wget -O roila_female.tar.gz https://github.com/joshhendo/RoilaLib/blob/master/files/voice/roila_female.tar.gz?raw=true
		CURRENT_PATH=`pwd`/roila_female
		echo $CURRENT_PATH
		tar -zxvf roila_female.tar.gz

		# ensure that the directory exists
		# http://stackoverflow.com/questions/59838/how-to-check-if-a-directory-exists-in-a-shell-script
		if [ -d "$INSTALL_LOCATION" ]; then
			sudo cp -R $CURRENT_PATH $INSTALL_LOCATION
		else
			echo "$INSTALL_LOCATION doesn't exist. Can't install voice until this location exists."
			echo "The voice has been downloaded and extracted locally without been installed."
		fi
	fi
}

function DownloadFestivalPortable {
	# TODO
	echo "Not implemented"
}


while [ true ]
do
	# Print a menu
	echo -e "Please select an option:\\n\\t1: Download pre-compiled Java Library\\n\\t2: Download Java Sources\\n\\t3: Download Sample Config\\n\\t4: Not Implemented\\n\\t5: Install Java (Portable)\\n\\t6: Install ROILA voice to Festival (admin required)\\n\\t0: Exit Script"
	read MENUOPTION

	if [ $MENUOPTION == "0" ]; then
		exit 0
	fi

	if [ $MENUOPTION == "1" ]; then
		echo "Downloading pre-compiled Java Library "
		DownloadJavaLib
	fi

	if [ $MENUOPTION == "2" ]; then
		echo "Downloading Java Sources"
		DownloadJavaSource
	fi

	if [ $MENUOPTION == "3" ]; then
		echo "Downloading Samples"
		DownloadSamples
	fi

	if [ $MENUOPTION == "4" ]; then
		echo "Not implemented"
	fi

	if [ $MENUOPTION == "5" ]; then
		echo "Downloading a portable version of the Java JDK."
		DownloadJava
	fi

	if [ $MENUOPTION == "6" ]; then
		echo "You have chosen to install the voice into Festival."
		echo -e "Which voice do you want?\\n\\t1: Male\\n\\t2: Female\\n\\t3: Both\\n\\t0: Cancel"
		read VOICEOPTION

		if [ $VOICEOPTION != "0" ]; then
			InstallVoice $VOICEOPTION
		fi

	fi

done
