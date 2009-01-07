#!/bin/bash

args=""

if [ $# -ne "0" ]
then
        url=$1
        if [ $1 == "-open" ]
        then
                url=$2
        fi

        args="-open $url"
fi

javaws http://www.blogbridge.com/install/weekly/blogbridge.jnlp $args
