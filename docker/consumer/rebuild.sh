docker stop qpan-consumer
docker rm qpan-consumer
docker rmi qpan-consumer
docker build -t qpan-consumer .
cd /opt/compose
docker-compose up -d
rm /opt/dockerFile/consumer/app.jar