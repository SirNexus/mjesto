#!/bin/bash

mongod &

npm run mongo --prefix /root/npm &

nginx

while [ true ]
do
    echo "In start script!"
    sleep 5
done