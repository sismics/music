package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.dao.dbi.dto.TrackDto;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class TrackDtoMapper implements ResultSetMapper<TrackDto> {
    @Override
    public TrackDto map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        TrackDto dto = new TrackDto();
        dto.setId(r.getString("id"));
        dto.setFileName(r.getString("fileName"));
        dto.setTitle(r.getString("title"));
        dto.setYear(r.getInt("year"));
        dto.setGenre(r.getString("genre"));
        dto.setLength(r.getInt("length"));
        dto.setBitrate(r.getInt("bitrate"));
        dto.setOrder(r.getInt("trackOrder"));
        dto.setVbr(r.getBoolean("vbr"));
        dto.setFormat(r.getString("format"));
        dto.setUserTrackPlayCount(r.getInt("userTrackPlayCount"));
        dto.setUserTrackLike(r.getBoolean("userTrackLike"));
        dto.setArtistId(r.getString("artistId"));
        dto.setArtistName(r.getString("artistName"));
        dto.setAlbumId(r.getString("albumId"));
        dto.setAlbumName(r.getString("albumName"));
        dto.setAlbumArt(r.getString("albumArt"));

        return dto;
    }
}
