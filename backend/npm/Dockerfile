FROM node:10

ADD start.sh /root

RUN apt-get update && apt-get install -y vim 

ADD . /root/npm

WORKDIR /root/npm

RUN npm install

RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv EA312927 && \
    echo "deb http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.2 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-3.2.list && \
    apt-get update && \
    apt-get install -y mongodb-org && \
    mkdir -p /data/db

EXPOSE 80

ENTRYPOINT ["/root/start.sh"]