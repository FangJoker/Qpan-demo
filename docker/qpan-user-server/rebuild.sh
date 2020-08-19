docker stop qpan-user-server
docker rm qpan-user-server
docker rmi qpan-user-server
docker build -t qpan-user-server .
cd /opt/compose
docker-compose up -d
rm /opt/dockerFile/qpan-user-server/app.jar