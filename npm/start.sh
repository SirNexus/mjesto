#!/bin/bash

mongod &

npm run mongo --prefix /root/npm &

nginx

while [ true ]
do
    echo "In start script!"
    sleep 500
done