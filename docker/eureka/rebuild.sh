docker stop eureka
docker rm eureka
docker rmi eureka
docker build -t eureka .
cd /opt/compose
docker-compose up -d