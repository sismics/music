FROM sismics/jetty:9.4.46
MAINTAINER benjamin.gam@gmail.com

RUN wget http://www.deb-multimedia.org/pool/main/d/deb-multimedia-keyring/deb-multimedia-keyring_2016.8.1_all.deb \
    && dpkg -i deb-multimedia-keyring_2016.8.1_all.deb

RUN echo "deb https://www.deb-multimedia.org bullseye main non-free" >> /etc/apt/sources.list \
  && apt-get update \
  && apt-get -y --force-yes -q install curl python3 ffmpeg

RUN curl -kL https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/youtube-dl \
  && chmod a+x /usr/local/bin/youtube-dl

COPY music-web/target/music-web-*.war /opt/jetty/webapps/music.war
COPY music-distribution-docker/music.xml /opt/jetty/webapps/music.xml
COPY music-distribution-docker/start.ini /opt/jetty/start.ini
