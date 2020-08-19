docker stop zuul
docker rm zuul
docker rmi zuul
docker build -t zuul .
cd /opt/compose
docker-compose up  -d