FROM nginx:1.15

RUN apt-get update && apt-get install -y vim

ADD index.html /static/index.html

ADD script.js /static/script.js

# ADD nginx.conf /etc/nginx/nginx.conf

WORKDIR /etc/nginx