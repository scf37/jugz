#!/bin/bash

export JAVA_OPTS="-Xms128m -Xmx128m \
    -XX:+UseParallelGC -XX:+PrintGCDetails -verbose:gc -XX:+PrintGCTimeStamps -Xloggc:/data/logs/gc$(date +%Y_%m_%d-%H_%M).log -Djava.net.preferIPv4Stack=true \
    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/ -XX:+ExitOnOutOfMemoryError"

mkdir -p /data/logs

exec /opt/bin/jugz $@