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

# Download Sphinx-4 beta 3
#wget -O $SPHINXDLNAME $SPHINXDL
#unzip $SPHINXDLNAME

# See if the user has supplied a custom word list to be compiled, otherwise grab it from roila.org

#TODO Check paramaters to see if there are any files supplied


function DownloadSamples {
	echo "Please input the location of your .6d file, or press ENTER to use default: "
	read SIXDFILE

	if [ $SIXDFILE != "" ]; then
		echo "Please input the location of the corresponding .lm file: "
		read LMFILE
	else
		# Download the default .6d and lm file
		wget http://dl.dropbox.com/u/4654434/9990.lm
		wget http://dl.dropbox.com/u/4654434/newdict2.6d

		SIXDFILE=`pwd`/newdict2.6d
		LMFILE=`pwd`/9990.lm
	fi

	wget http://dl.dropbox.com/u/4654434/roila.config.xml

	LMFILE=`echo $LMFILE | sed -e 's/[\/&]/\\\\&/g'`
	SIXDFILE=`echo $SIXDFILE | sed -e 's/[\/&]/\\\\&/g'`

	eval sed -i \'s/D:\\\\java\\\\LEGO_TEST_PC\\\\src\\\\9127.lm/$LMFILE/g\' roila.config.xml
	eval sed -i \'s/D:\\\\java\\\\LEGO_TEST_PC\\\\src\\\\newdict2.6d/$SIXDFILE/g\' roila.config.xml
}

#wget -O BTsend.class http://roila.org/wp-content/uploads/2010/04/roila_java.txt

# Replace paths in roila.config.xml with the appropriate paths.
# D:\java\LEGO_TEST_PC\src\9127.lm with $LMFILE
# D:\java\LEGO_TEST_PC\src\newdict2.6d with $6DFILE

#sed -i -e 'D:\java\LEGO_TEST_PC\src\9127.lm' -e '$LMFILE' roila.config.xml
#sed -i -e 'D:\java\LEGO_TEST_PC\src\newdict2.6d' -e '$SIXDFILE' roila.config.xml

# Escape the file properly. see http://stackoverflow.com/questions/471183/linux-command-line-global-search-and-replace


# Print a menu
echo -e "Please select an option:\\n\\t1: Download pre-compiled Java Library with Sample Config\\n\\t2: Download Java Sources with Samples"
read MENUOPTION

if [ $MENUOPTION == "1" ]; then
	echo "You selected option 1!"
	DownloadSamples
fi

if [ $MENUOPTION == "2" ]; then
	echo "You selected option 2!"
	DownloadSamples
fi

exit 0