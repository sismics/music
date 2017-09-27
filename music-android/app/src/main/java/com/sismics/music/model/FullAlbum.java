package com.sismics.music.model;

public class FullAlbum {
    private Artist artist;
    private Album album;

    public FullAlbum(Artist artist, Album album) {
        this.artist = artist;
        this.album = album;
    }

    public Artist getArtist() {
        return artist;
    }

    public Album getAlbum() {
        return album;
    }

    @Override
    public int hashCode() {
        return album.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FullAlbum
                && ((FullAlbum) obj).album.getId().equals(album.getId());
    }
}
