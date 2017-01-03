FROM sismics/jetty:9.2.20-jdk7
MAINTAINER benjamin.gam@gmail.com

RUN echo "deb http://www.deb-multimedia.org jessie main non-free" >> /etc/apt/sources.list \
  && echo "deb-src http://www.deb-multimedia.org jessie main non-free" >> /etc/apt/sources.list \
  && apt-get update \
  && apt-get -y --force-yes -q install ffmpeg curl python

RUN curl -L https://yt-dl.org/downloads/latest/youtube-dl -o /usr/local/bin/youtube-dl \
  && chmod a+x /usr/local/bin/youtube-dl

ADD music-web/target/music-web-*.war /opt/jetty/webapps/music.war
ADD music.xml /opt/jetty/webapps/music.xml
