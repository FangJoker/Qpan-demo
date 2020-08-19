docker stop qpan-io-server
docker rm qpan-io-server
docker rmi qpan-io-server
docker build -t qpan-io-server .
cd /opt/compose
docker-compose up -d
