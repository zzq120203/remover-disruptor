#!/bin/bash

# 通用重启服务脚本
# 
# email : zzq120203@163.com
# v3.0
#``````````````````````````
#     ┏┓　　　┏┓
#    ┏┛┻━━━━━━┛┻┓
#    ┃          ┃
#    ┃     ━    ┃
#    ┃  ┳┛   ┗┳ ┃
#    ┃          ┃
#    ┃     ┻    ┃
#    ┃          ┃
#    ┗━┓      ┏━┛
#      ┃      ┃神兽保佑
#      ┃      ┃代码无BUG！
#      ┃      ┗━━━━━┓
#      ┃            ┣┓
#      ┃            ┃
#      ┗━┓┓┏━━━━┳┓┏━┛
#        ┃┫┫    ┃┫┫
#        ┗┻┛    ┗┻┛
#^^^^^^^^^^^^^^^^^^^^^^^^^^


##参数
SERVICEHOME="/home/remover/service/"
cd $SERVICEHOME
# jar
JARName="remover.jar"
# 程序主类名
MCName="Remover"
# 程序主类路径 "."为分隔符
MCPath="cn.ac.iie.remover"
# 第三方包路径
libPath="lib"
# library路径
libraryPath="library"
# java参数
JAVAVS="/jdk1.8.0_131"
JAVA="../"$JAVAVS"/bin/java"
JPS="../"$JAVAVS"/bin/jps"
JAVAC="../"$JAVAVS"/bin/javac"
JAR="../"$JAVAVS"/bin/jar"
# Make
src="cn"
MAKE_JAR="remover.jar"
build="build"
# 其他参数
confPath="configs/config.txt"
log4jPath="configs/log4j.properties"
logsPath="logs"
logName="rem.log"


function __status() {
    pid=`$JPS | grep "$MCName" | awk  '{print $1}'`
    if [ -n "$pid" ]
    then
        echo -e "\033[42;20m$MCName is runing ==> PID:$pid\033[0m"
        logtimestr=`tail -n 1 $logsPath"/"$logName | awk  -F '[' '{print $2}' | awk -F ' INFO| ERROR' '{print $1}'`
        logtime=`date -d "$logtimestr" +%s`
        newtime=`date -d -5minute +%s`
        if [ $newtime -gt $logtime ]
        then
            echo -e "\033[41;20m`tail -n 1 $logsPath"/"$logName`\033[0m"
        else
            tail -n 1 $logsPath"/"$logName
        fi
    else
        echo -e "\033[41;20m$MCName is not runing\033[0m"
    fi
}

function __start() {
    pid=`$JPS | grep "$MCName" | awk  '{print $1}'`
    if [ -n "$pid" ]
    then
        echo "$MCName is runing ==> PID:$pid"
    else
        if [ ! -f "$JARName" ]; then
            echo "ERROR:$JARName is not exist"
            exit
        fi    
        
        for i in `ls $libPath`;
        do 
            libs+="$libPath/$i:"
        done

        nohup $JAVA -Djava.library.path=$libraryPath -Dconfig=$confPath -Dlog4j.configuration=$log4jPath -Xms10g -Xmx20g -XX:+UseG1GC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:$logsPath/gc.log -cp $libs:$JARName $MCPath$MCName >> $logsPath/$MCName.log 2>&1 &
        echo "$MCName ==> PID:`$JPS | grep "$MCName" | awk  '{print $1}'`; JAR:$JARName"

    fi
}

function __stop() {
    pid=`$JPS | grep "$MCName" | awk  '{print $1}'`
    if [ -n "$pid" ]
    then
        kill $pid
        echo "Kill $MCName ==> PID:$pid"
    else
        echo "$MCName is not running"
    fi
}

function __reboot() {
    __stop;
    
    i=`$JPS | grep "$MCName" | wc -l`
    while [[ "$i" -ne 0 ]]
    do
        sleep 1
        echo "Wait until $MCName stops ==> PID:$pid" 
        i=`$JPS | grep "$MCName" | wc -l`
    done
    
    __start;
}

function __make() {
    for i in `ls $libPath`;
        do
            libs+="$libPath/$i:"
        done
    find $src -name '*.java' > sourcelist
    $JAVAC -cp $libs @sourcelist -d $build
    rm sourcelist
    cd $build
    "../"$JAR -cvf $MAKE_JAR $src
}

function __help() {
    echo "make   [jar] : make java project"
    echo "status       : service status"
    echo "start  [jar] : start service"
    echo "stop         : stop service"
    echo "reboot [jar] : reboot service"
    exit
}


if [ "x$1" == "x" ]; then
    echo "ERROR:Parameter cannot be null"
    __help;
elif [ "x$1" == "xstatus" ]; then
    __status;
elif [ "x$1" == "xstart" ]; then
    echo "Start $MCName"
    if [ ! "x$2" == "x" ]; then
        JARName=$2
    fi
    __start;
elif [ "x$1" == "xreboot" ]; then
    echo "Reboot $MCName"
    if [ ! "x$2" == "x" ]; then
        JARName=$2
    fi
    __reboot;
elif [ "x$1" == "xstop" ]; then
    echo "Stop $MCName"
    __stop;
elif [ "x$1" == "xmake" ]; then
    if [ ! "x$2" == "x" ]; then
        MAKE_JAR=$2
    fi
    echo "Make $MAKE_JAR"
    __make;
else
    echo "ERROR:Parameter error"
    __help;
fi
