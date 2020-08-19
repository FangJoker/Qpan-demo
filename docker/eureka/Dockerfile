FROM java:8
VOLUME /tmp
ENV VIRTUAL_PORT 8761
ENV VIRTUAL_HOST 120.78.74.109/eureka
ENV LETSENCRYPT_HOST 120.78.74.109/eureka
MAINTAINER chavez
EXPOSE 8761
ADD  app.jar app.jar
# ÔËÐÐjar°ü
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]