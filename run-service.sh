#!/bin/bash
docker rm -f sismics_music
docker run \
    -d --name=sismics_music --restart=always \
    -v sismics_music_data:/data \
    -e 'VIRTUAL_HOST=music.sismics.com' -e 'VIRTUAL_PORT=80' \
    sismics/music:latest
