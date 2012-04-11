#!/bin/bash

BASE_TMP_DIR="$(dirname $0)/../../tmp/"

function change_dir {
    echo "Changing dir to $1 ..."
    if ! cd ${1}; then
	echo "Failed to cd to '${1}'"
	exit 1
    fi
    echo "Current directory: $(pwd)"
}

# create tmp subdir in the current directory and change to it
function create_subdir_n_cd {
    TMPDIR=$(mktemp -d --tmpdir="$(pwd)")
    echo "Created temp dir '${TMPDIR}'"
    change_dir "${TMPDIR}"
}

# build with ant (target may be passed as $1)
function run_ant {
    echo "Trying to invoke ant with target '$1' ..."
    if ! which ant; then
	echo "Ant not found in path"
	exit 1
    fi
    if ! ant $1; then
	echo "Build failed"
	exit 1
    fi
    echo "Execution of ant failed"
}

# provide content list of archive
function print_archive_contents {
    if [ -z "$1" ]; then
	echo "No archive provided"
	exit 1
    fi
    
    if echo "$1" | grep "zip"; then
	unzip -l "$1" | awk '{ print $4 }'
	return
    fi

    if echo "$1" | grep "tar.gz"; then
	tar -tzf "$1"
	return
    fi
}

# extract archiv and change into directory
function extract_archive_n_cd {
    if [ -z "$1" ]; then
	echo "No archive provided"
	exit 1
    fi

    if echo "$1" | grep "zip"; then
	unzip -q "$1" 
    elif echo "$1" | grep "tar.gz"; then
	tar -xzf "$1"
    else
	echo "Archive '$1' is neither zip nor .tar.gz"
	exit 1
    fi 

    change_dir kieker-*
}

function assert_file_NOT_exists {
    echo -n "Asserting file '$1' does not exist ..."
    if test -e "$1"; then
	echo "File '$1' should not exist"
	exit 1
    fi
    echo OK
}

function assert_dir_exists {
    echo -n "Asserting '$1' is a directory ..."
    if ! test -d "$1"; then
	echo "File '$1' is missing or not a directory"
	exit 1
    fi
    echo OK
}

function assert_file_exists_regular {
    echo -n "Asserting '$1' is a regular file ..."
    if ! test -s "$1"; then
	echo "File '$1' is missing or not a regular file"
	exit 1
    fi
    echo OK
}

# Asserts the existence of files common to the src and bin releases
function assert_files_exist_common {
    assert_dir_exists "bin/"
    assert_dir_exists "doc/"
    assert_dir_exists "examples/"
    assert_dir_exists "lib/"
    assert_file_exists_regular "HISTORY"
    assert_file_exists_regular "LICENSE"
    assert_file_NOT_exists "build/"
    assert_file_NOT_exists "build-eclipse/"
    assert_file_NOT_exists "tmp/"
    assert_file_NOT_exists ".git/"
    assert_file_NOT_exists ".gitignore/"
    # check if LICENSE file for each jar
    for jar in $(find lib/ -name "*.jar"); do
	if (echo "$jar" | grep "noUpdateChecks"); then
	    continue # exclude  noUpdateChecks
	fi
	JAR_BASE=$(echo ${jar} | sed 's/\(.*\)\..*/\1/') # remove file extension
	assert_file_exists_regular "${JAR_BASE}.LICENSE"
    done
}

# Asserts the existence of files common to the src and bin releases
function assert_files_exist_src {
    assert_files_exist_common
    assert_dir_exists "model/"
    assert_dir_exists "src/"
    assert_dir_exists "src-gen/"
    assert_dir_exists "test/"
    assert_file_NOT_exists "dist/"
    assert_file_NOT_exists "META-INF/"
    assert_file_exists_regular "kieker-eclipse-cleanup.xml"
    assert_file_exists_regular "kieker-eclipse-formatter.xml"
    assert_file_exists_regular ".project"
    assert_file_exists_regular ".classpath"
}

# Asserts the existence of files common to the src and bin releases
function assert_files_exist_bin {
    assert_files_exist_common
    assert_file_exists_regular "doc/kieker-"*"_userguide.pdf"
    assert_dir_exists "dist/"
    MAIN_JAR=$(ls "dist/kieker-"*".jar" | grep -v emf | grep -v aspectj )
    assert_file_exists_regular ${MAIN_JAR}
    assert_file_exists_regular "dist/kieker-"*"_aspectj.jar"
    assert_file_exists_regular "dist/kieker-"*"_emf.jar"
    assert_file_exists_regular "dist/kieker-monitoring-servlet-"*".war"
    assert_file_NOT_exists "dist/release/"
    assert_file_NOT_exists "bin/dev/"
    assert_file_NOT_exists "doc/userguide/"
    assert_file_NOT_exists "model/"
    assert_file_NOT_exists "src/"
    assert_file_NOT_exists "src-gen/"
    assert_file_NOT_exists "test/"
    assert_file_NOT_exists "kieker-eclipse-cleanup.xml"
    assert_file_NOT_exists "kieker-eclipse-formatter.xml"
    assert_file_NOT_exists ".project"
    assert_file_NOT_exists ".classpath"
}

function check_src_archive {
    if [ -z "$1" ]; then
	echo "No source archive provided"
	exit 1
    fi

    echo "Checking source archives '$1' ..."

    echo "Checking archive contents of '$1' ..."
    ARCHIVE_CONTENT=$(print_archive_contents "$1")
    # TODO: do s.th. with the ${ARCHIVE_CONTENT}

    echo "Decompressing archive '$1' ..."
    extract_archive_n_cd "$1"
    touch $(basename "$1") # just to mark where this dir comes from

    assert_files_exist_src

    # now compile sources
    run_ant
    # make sure that the expected files are present
    assert_dir_exists "dist/"
    assert_file_exists_regular $(ls "dist/kieker-"*".jar" | grep -v emf | grep -v aspectj ) # the core jar
    assert_file_exists_regular "dist/kieker-"*"_aspectj.jar"
    assert_file_exists_regular "dist/kieker-"*"_emf.jar"
    assert_file_exists_regular "dist/kieker-monitoring-servlet-"*".war"

    # check bytecode version of classes contained in jar
    echo -n "Making sure that bytecode version of class in jar is 49.0 (Java 1.5)"
    MAIN_JAR=$(ls "dist/kieker-"*".jar" | grep -v emf | grep -v aspectj)
    assert_file_exists_regular ${MAIN_JAR}
    VERSION_CLASS=$(find build -name "Version.class")
    assert_file_exists_regular "${VERSION_CLASS}"
    if ! file ${VERSION_CLASS} | grep "version 49.0 (Java 1.5)"; then
	echo "Unexpected bytecode version"
	exit 1
    fi
    echo "OK"

    # now execute junt tests (which compiles the sources again ...)
    run_ant run-tests-junit

    # now execute junt tests (which compiles the sources again ...)
    run_ant static-analysis
}

function check_bin_archive {
    if [ -z "$1" ]; then
	echo "No source archive provided"
	exit 1
    fi

    echo "Checking source archives '$1' ..."

    echo "Checking archive contents of '$1' ..."
    ARCHIVE_CONTENT=$(print_archive_contents "$1")
    # TODO: do s.th. with the ${ARCHIVE_CONTENT}

    echo "Decompressing archive '$1' ..."
    extract_archive_n_cd "$1"
    touch $(basename "$1") # just to mark where this dir comes from

    assert_files_exist_bin

    # check bytecode version of classes contained in jar
    echo -n "Making sure that bytecode version of class in jar is 49.0 (Java 1.5)"
    MAIN_JAR=$(ls "dist/kieker-"*".jar" | grep -v emf | grep -v aspectj)
    assert_file_exists_regular ${MAIN_JAR}
    VERSION_CLASS_IN_JAR=$(unzip -l  ${MAIN_JAR} | grep Version.class | awk '{ print $4 }')
    unzip "${MAIN_JAR}" "${VERSION_CLASS_IN_JAR}"
    assert_file_exists_regular "${VERSION_CLASS_IN_JAR}"
    if ! file ${VERSION_CLASS_IN_JAR} | grep "version 49.0 (Java 1.5)"; then
	echo "Unexpected bytecode version"
	exit 1
    fi
    echo "OK"

    # some basic tests with the tools
    if ! (bin/convertLoggingTimestamp.sh --timestamps 1283156545581511026 1283156546127117246 | grep "Mon, 30 Aug 2010 08:22:25 +0000 (UTC)"); then
	echo "Unexpected result executinǵ bin/convertLoggingTimestamp.sh"
	exit 1
    fi
    

    # now perform some trace analysis tests and compare results with reference data
    ARCHDIR=$(pwd)
    create_subdir_n_cd
    REFERENCE_OUTPUT_DIR="${ARCHDIR}/examples/userguide/ch5--trace-monitoring-aspectj/testdata/kieker-20100830-082225522-UTC-example-plots"
    PLOT_SCRIPT="${ARCHDIR}/examples/userguide/ch5--trace-monitoring-aspectj/testdata/kieker-20100830-082225522-UTC-example-plots.sh"
    if ! test -x ${PLOT_SCRIPT}; then
	echo "${PLOT_SCRIPT} does not exist or is not executable"
	exit 1
    fi
    if ! ${PLOT_SCRIPT} "${ARCHDIR}" "."; then # passing kieker dir and output dir
	echo "${PLOT_SCRIPT} returned with error"
	exit 1
    fi
    for f in $(ls "${REFERENCE_OUTPUT_DIR}" | egrep "(dot$|pic$|html$|txt$)"); do 
	echo -n "Comparing to reference file $f ... "
	# Note that this is a hack because sometimes the line order differs
	(cat "$f" | sort) > left.tmp
	(cat "${REFERENCE_OUTPUT_DIR}/$f" | sort) > right.tmp
	if ! diff "$f" "${REFERENCE_OUTPUT_DIR}/$f"; then
	    echo "Detected deviation between files: '$f', '${${REFERENCE_OUTPUT_DIR}/$f}'"
	    exit 1
	else 
	    echo "OK"
	fi
    done

    # Return to archive base dir
    cd ${ARCHDIR}

    # TODO: test examples ...
}

##
## "main" 
##
assert_dir_exists ${BASE_TMP_DIR}
change_dir "${BASE_TMP_DIR}"
BASE_TMP_DIR_ABS=$(pwd)

#change_dir "${BASE_TMP_DIR_ABS}"
#create_subdir_n_cd
#DIR=$(pwd)
#SRCZIP=$(ls ../../dist/release/*_sources.zip)
#assert_file_exists_regular ${SRCZIP}
#check_src_archive "${SRCZIP}"
#rm -rf ${DIR}

#change_dir "${BASE_TMP_DIR_ABS}"
#create_subdir_n_cd
#DIR=$(pwd)
#SRCTGZ=$(ls ../../dist/release/*_sources.tar.gz)
#assert_file_exists_regular ${SRCTGZ}
#check_src_archive "${SRCTGZ}"
#rm -rf ${DIR}

#change_dir "${BASE_TMP_DIR_ABS}"
#create_subdir_n_cd
#DIR=$(pwd)
#BINZIP=$(ls ../../dist/release/*_binaries.zip)
#assert_file_exists_regular ${BINZIP}
#check_bin_archive "${BINZIP}"
#rm -rf ${DIR}

change_dir "${BASE_TMP_DIR_ABS}"
create_subdir_n_cd
DIR=$(pwd)
BINTGZ=$(ls ../../dist/release/*_binaries.tar.gz)
assert_file_exists_regular ${BINTGZ}
check_bin_archive "${BINTGZ}"
rm -rf ${DIR}

# TOOD: check contents of remaining archives


