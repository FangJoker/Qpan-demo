FROM java:8
VOLUME /tmp
ENV VIRTUAL_PORT 6069
ENV VIRTUAL_HOST 120.78.74.109/zuul
ENV LETSENCRYPT_HOST 120.78.74.109/zuul
MAINTAINER chavez
EXPOSE 6069
ADD  app.jar app.jar
# ÔËÐÐjar°ü
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]