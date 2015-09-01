#!/bin/bash

if [ "$1" = '-help' ]; then
	echo "command like---->: srouce ScriptPNG.sh param"
	echo "---------------->: param is the direcotry where pngs locate"
	return
fi

if [ ! -n "$1" ]; then
	echo 'please input correct command, more info by "-help"'
	return
fi


originalDir=$1
backupDir=$1".backup"
rm -rf $backupDir
mkdir $backupDir
cp -r $originalDir $backupDir
rm log.txt
#echo $originalDir
for fileName in $(find $originalDir -name "*.png")
do
	echo ' ---------------- ' 1>>log.txt
    echo 'fileName->'$fileName 1>>log.txt
    #step 1 :
    wine ./lib/truepng -f0,5 -i0 -g0 -a0 -md remove all -zc7 -zm4-9 -zs0-3 -force -y $fileName 1>temp
    zmAndZs=`tail -2 temp | awk '{print $2} {print $3}' | awk -F ':' '{print $2}'`
    arr=$(echo $zmAndZs | tr " " "\n")
    count=0
    for item in $arr; do
    	let count++
    	if [ $count -eq 1 ]; then
    		let memlevel=item
    		echo "zlib-memlevel->"$memlevel 1>>log.txt
    	elif [ $count -eq 2 ]; then
    		let strategy=item
    		echo "zlib-strategy->"$strategy 1>>log.txt
    	fi
    done

    if [ $count -ne 2 ]; then
    	echo can not get zlib-memlevel and zlib-strategy 1>>log.txt
    	return
    fi

    #step 2 : 
    pnginfo=`wine ./lib/pngout -l $fileName | awk '{print $1}'`
    colortypes=$(echo $pnginfo | tr "c" "\n")
    let count=0
    for item in $colortypes; do
    	let count++
    	if [ $count -eq 2 ]; then
    		let colortype=item
    		echo "colortype->"$colortype 1>>log.txt
    	fi
    done

    if [ $count -ne 2 ]; then
    	echo can not get colortype 1>>log.txt
    	return
    fi

    #step 3 :
    if [ $colortype -eq 6 ]; then
        wine ./lib/cryopng -for -q -zc7 -zm1 -zs1 -f1 $fileName
    fi

    zip_compression=2
    fileSize=$(ls -ld $fileName | awk '{print int($5)}')
    echo "fileSize->"$fileSize 1>>log.txt
    if [ $fileSize -ge 65536 ]; then
        let zip_compression=1
    elif [ $fileSize -le 16384 ]; then
        let zip_compression=4
    fi
    echo "zip_compression->"$zip_compression 1>>log.txt

    wine ./lib/pngwolf --in=$fileName --out=$fileName --max-stagnate-time=0 --max-evaluations=1 --zlib-level=7 --zlib-strategy=$strategy --zlib-window=15 --zlib-memlevel=$memlevel --7zip-mpass=$zip_compression --even-if-bigger
    wine ./lib/deflopt -k -b -s $fileName

    #step 4 : 
    #    compare fileSize and choose the best png
	afterFileSize=$(ls -ld $fileName | awk '{print int($5)}')
	beforeFileSize=$(ls -ld $backupDir'/'$fileName | awk '{print int($5)}')
	reduceSize=`expr $beforeFileSize - $afterFileSize`
	ratio=$(echo "scale=2; $reduceSize*100.0/$beforeFileSize*1.0" | bc)
	if [ $reduceSize -gt 0 ]; then
		echo "reduce size->"$reduceSize 1>>log.txt
		echo "compress ratio->"$ratio"%" 1>>log.txt
		if [ $afterFileSize -eq 0 ]; then
			cp -f $backupDir'/'$fileName $fileName
		fi
	else
		cp -f $backupDir'/'$fileName $fileName
		echo "can not reduce->"$fileName 1>>log.txt
	fi

	#echo $(echo "scale=5; 5*1.0/6*1.0" | bc)

	echo ' ---------------- ' 1>>log.txt
done