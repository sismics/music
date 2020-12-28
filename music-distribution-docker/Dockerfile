FROM sismics/jetty:9.3.11
MAINTAINER benjamin.gam@gmail.com

RUN echo "deb http://www.deb-multimedia.org jessie main non-free" >> /etc/apt/sources.list \
  && echo "deb-src http://www.deb-multimedia.org jessie main non-free" >> /etc/apt/sources.list \
  && apt-get update \
  && apt-get -y --force-yes -q install ffmpeg curl python

RUN curl -kL https://yt-dl.org/downloads/latest/youtube-dl -o /usr/local/bin/youtube-dl \
  && chmod a+x /usr/local/bin/youtube-dl

COPY music-web/target/music-web-*.war /opt/jetty/webapps/music.war
COPY music-distribution-docker/music.xml /opt/jetty/webapps/music.xml
COPY music-distribution-docker/start.ini /opt/jetty/start.ini
