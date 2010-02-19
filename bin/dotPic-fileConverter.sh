#!/bin/bash

#
# For all .dot and .pic files in the directory passed
# as parameter $1, this script generates image files
# in the format passed as $2..$n by calling the 
# tools dot and pic2plot.
#
# Example: $ dotPic-fileConverter tmp/ svg png ps
#
# @author Andre van Hoorn


if [ ! -d "$1" ]; then
    echo "'$1' is not a directory"
    exit 1
fi

if [ -z "$2" ]; then
    echo "Missing file extensions"
    exit 1
fi

DIRNAME="$1"
shift

PIC_FILES=$(find ${DIRNAME} -name "*.pic")
DOT_FILES=$(find ${DIRNAME} -name "*.dot")

PIC_COUNTER=0
DOT_COUNTER=0

EXTS=$*

for f in ${PIC_FILES}; do 
    BASENAME=$(echo $f | sed -E s/"\.[[:alnum:]]+$"//g); 
    if (echo "${EXTS}" | grep -q pdf) && !(echo "$*" | grep -q ps); then
	EXTS="$EXTS ps"
    fi
    for ext in ${EXTS}; do 
	if !(echo "${ext}" | grep -q pdf); then
	    pic2plot -T ${ext} $f > ${BASENAME}.${ext} ;  
	fi
    done; 
    if (echo "${EXTS}" | grep -q pdf); then
	ps2pdf ${BASENAME}.ps ${BASENAME}.pdf \
	    && (pdfcrop ${BASENAME}.pdf > /dev/null) \
	    && rm ${BASENAME}.pdf \
	    && mv ${BASENAME}-crop.pdf ${BASENAME}.pdf
    fi
    PIC_COUNTER=$((${PIC_COUNTER}+1))
done

if [ ! -z "${DOT_FILES}" ]; then
    for f in ${DOT_FILES}; do 
	BASENAME=$(echo $f | sed -E s/'\.[[:alnum:]]+$'//g); 
	for ext in ${EXTS}; do 
	    dot -T ${ext} ${f} > ${BASENAME}.${ext} ; 
	    if (echo "${ext}" | grep -q pdf); then
		(pdfcrop ${BASENAME}.pdf > /dev/null) \
		    && rm ${BASENAME}.pdf \
		    && mv ${BASENAME}-crop.pdf ${BASENAME}.pdf
	    fi
	done; 
	DOT_COUNTER=$((${DOT_COUNTER}+1))
    done
fi

echo 
echo "Processed ${DOT_COUNTER} .dot files"
echo "Processed ${PIC_COUNTER} .pic files"
