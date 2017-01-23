package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.dao.dbi.mapper.TrackDtoMapper;
import com.sismics.music.core.dao.dbi.mapper.TrackMapper;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.music.core.util.dbi.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.BaseDao;
import com.sismics.util.dbi.filter.FilterCriteria;
import org.skife.jdbi.v2.Handle;

import java.sql.Timestamp;
import java.util.*;

/**
 * Track DAO.
 * 
 * @author jtremeaux
 */
public class TrackDao extends BaseDao<TrackDto, TrackCriteria> {
    @Override
    protected QueryParam getQueryParam(TrackCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select t.TRK_ID_C as id, t.TRK_FILENAME_C as fileName, t.TRK_TITLE_C as title, t.TRK_YEAR_N as year, t.TRK_GENRE_C as genre, t.TRK_LENGTH_N as length, t.TRK_BITRATE_N as bitrate, t.TRK_ORDER_N as trackOrder, t.TRK_VBR_B as vbr, t.TRK_FORMAT_C as format,");
        if (criteria.getUserId() != null) {
            sb.append(" ut.UST_PLAYCOUNT_N as userTrackPlayCount, ut.UST_LIKE_B userTrackLike, ");
        } else {
            sb.append(" 0 as userTrackPlayCount, false as userTrackLike, ");
        }
        sb.append(" a.ART_ID_C as artistId, a.ART_NAME_C as artistName, t.TRK_IDALBUM_C as albumId, alb.ALB_NAME_C as albumName, alb.ALB_ALBUMART_C as albumArt");
        if (criteria.getPlaylistId() != null) {
            sb.append("  from T_PLAYLIST_TRACK pt, T_TRACK t ");
        } else {
            sb.append("  from T_TRACK t ");
        }
        sb.append("  join T_ARTIST a ON(a.ART_ID_C = t.TRK_IDARTIST_C and ART_DELETEDATE_D is null) ");
        sb.append("  join T_ALBUM alb ON(t.TRK_IDALBUM_C = alb.ALB_ID_C and alb.ALB_DELETEDATE_D is null) ");
        if (criteria.getUserId() != null) {
            sb.append("  left join T_USER_TRACK ut ON(ut.UST_IDTRACK_C = t.TRK_ID_C and ut.UST_IDUSER_C = :userId and ut.UST_DELETEDATE_D is null) ");
        }

        // Adds search criteria
        criteriaList.add("t.TRK_DELETEDATE_D is null");
        if (criteria.getPlaylistId() != null) {
            criteriaList.add("pt.PLT_IDTRACK_C = t.TRK_ID_C");
            criteriaList.add("pt.PLT_IDPLAYLIST_C = :playlistId");
            parameterMap.put("playlistId", criteria.getPlaylistId());
        }
        if (criteria.getAlbumId() != null) {
            criteriaList.add("t.TRK_IDALBUM_C = :albumId");
            parameterMap.put("albumId", criteria.getAlbumId());
        }
        if (criteria.getDirectoryId() != null) {
            criteriaList.add("alb.ALB_IDDIRECTORY_C = :directoryId");
            parameterMap.put("directoryId", criteria.getDirectoryId());
        }
        if (criteria.getArtistId() != null) {
            criteriaList.add("a.ART_ID_C = :artistId");
            parameterMap.put("artistId", criteria.getArtistId());
        }
        if (criteria.getTitle() != null) {
            criteriaList.add("lower(t.TRK_TITLE_C) like lower(:title)");
            parameterMap.put("title", criteria.getTitle());
        }
        if (criteria.getArtistName() != null) {
            criteriaList.add("lower(a.ART_NAME_C) like lower(:artistName)");
            parameterMap.put("artistName", criteria.getArtistName());
        }
        if (criteria.getLike() != null) {
            criteriaList.add("(lower(t.TRK_TITLE_C) like lower(:like) or lower(alb.ALB_NAME_C) like lower(:like) or lower(a.ART_NAME_C) like lower(:like))");
            parameterMap.put("like", "%" + criteria.getLike() + "%");
        }
        if (criteria.getUserId() != null) {
            parameterMap.put("userId", criteria.getUserId());
        }

        SortCriteria sortCriteria;
        if (criteria.getPlaylistId() != null) {
            sortCriteria = new SortCriteria(" order by pt.PLT_ORDER_N asc");
        } else if (criteria.getLike() != null || criteria.getArtistId() != null) {
            sortCriteria = new SortCriteria(" order by alb.ALB_NAME_C, t.TRK_ORDER_N, t.TRK_TITLE_C asc");
        } else if (criteria.getRandom() != null && criteria.getRandom()) {
            sortCriteria = new SortCriteria(" order by rand()");
        } else {
            sortCriteria = new SortCriteria(" order by t.TRK_ORDER_N, t.TRK_TITLE_C asc");
        }

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new TrackDtoMapper());
    }

    /**
     * Creates a new track.
     * 
     * @param track Track to create
     * @return Track ID
     */
    public String create(Track track) {
        track.setId(UUID.randomUUID().toString());
        track.setCreateDate(new Date());

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                "  T_TRACK(TRK_ID_C, TRK_IDALBUM_C, TRK_IDARTIST_C, TRK_FILENAME_C, TRK_TITLE_C, TRK_TITLECORRECTED_C, TRK_YEAR_N, TRK_GENRE_C, TRK_LENGTH_N, TRK_BITRATE_N, TRK_ORDER_N, TRK_VBR_B, TRK_FORMAT_C, TRK_CREATEDATE_D)" +
                "  values(:id, :albumId, :artistId, :fileName, :title, :titleCorrected, :year, :genre, :length, :bitrate, :order, :vbr, :format, :createDate)")
                .bind("id", track.getId())
                .bind("albumId", track.getAlbumId())
                .bind("artistId", track.getArtistId())
                .bind("fileName", track.getFileName())
                .bind("title", track.getTitle())
                .bind("titleCorrected", track.getTitleCorrected())
                .bind("year", track.getYear())
                .bind("genre", track.getGenre())
                .bind("length", track.getLength())
                .bind("bitrate", track.getBitrate())
                .bind("order", track.getOrder())
                .bind("vbr", track.isVbr())
                .bind("format", track.getFormat())
                .bind("createDate", new Timestamp(track.getCreateDate().getTime()))
                .execute();

        return track.getId();
    }
    
    /**
     * Updates a track.
     * 
     * @param track Track to update
     * @return Updated track
     */
    public Track update(Track track) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_TRACK t set " +
                " t.TRK_IDALBUM_C = :albumId, " +
                " t.TRK_IDARTIST_C = :artistId, " +
                " t.TRK_FILENAME_C = :fileName, " +
                " t.TRK_TITLE_C = :title, " +
                " t.TRK_TITLECORRECTED_C = :titleCorrected, " +
                " t.TRK_YEAR_N = :year, " +
                " t.TRK_GENRE_C = :genre, " +
                " t.TRK_LENGTH_N = :length, " +
                " t.TRK_BITRATE_N = :bitrate, " +
                " t.TRK_ORDER_N = :order, " +
                " t.TRK_VBR_B = :vbr, " +
                " t.TRK_FORMAT_C = :format, " +
                " t.TRK_CREATEDATE_D = :createDate " +
                " where t.TRK_ID_C = :id and t.TRK_DELETEDATE_D is null")
                .bind("id", track.getId())
                .bind("albumId", track.getAlbumId())
                .bind("artistId", track.getArtistId())
                .bind("fileName", track.getFileName())
                .bind("title", track.getTitle())
                .bind("titleCorrected", track.getTitleCorrected())
                .bind("year", track.getYear())
                .bind("genre", track.getGenre())
                .bind("length", track.getLength())
                .bind("bitrate", track.getBitrate())
                .bind("order", track.getOrder())
                .bind("vbr", track.isVbr())
                .bind("format", track.getFormat())
                .bind("createDate", new Timestamp(track.getCreateDate().getTime()))
                .execute();

        return track;
    }

    /**
     * Gets an active track by its file name.
     * 
     * @param directoryId Directory ID
     * @param fileName Track file name
     * @return Track
     */
    public Track getActiveByDirectoryAndFilename(String directoryId, String fileName) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new TrackMapper().getJoinedColumns("t") +
                "  from T_TRACK t, T_ALBUM a" +
                "  where t.TRK_FILENAME_C = :fileName and t.TRK_DELETEDATE_D is null " +
                "  and a.ALB_ID_C = t.TRK_IDALBUM_C and a.ALB_IDDIRECTORY_C = :directoryId and a.ALB_DELETEDATE_D is null")
                .bind("directoryId", directoryId)
                .bind("fileName", fileName)
                .mapTo(Track.class)
                .first();
    }
    
    /**
     * Gets active tracks included in a location.
     * 
     * @param directoryId Directory ID
     * @param location Parent location
     * @return List of tracks
     */
    public List<Track> getActiveByDirectoryInLocation(String directoryId, String location) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new TrackMapper().getJoinedColumns("t") +
                "  from T_TRACK t, T_ALBUM a" +
                "  where locate(:location, t.TRK_FILENAME_C) = 1 and t.TRK_DELETEDATE_D is null " +
                "  and a.ALB_ID_C = t.TRK_IDALBUM_C and a.ALB_IDDIRECTORY_C = :directoryId and a.ALB_DELETEDATE_D is null")
                .bind("directoryId", directoryId)
                .bind("location", location)
                .mapTo(Track.class)
                .list();
    }
    
    /**
     * Gets an active track by its trackname.
     *
     * @param id Track ID
     * @return Track
     */
    public Track getActiveById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new TrackMapper().getJoinedColumns("t") +
                "  from T_TRACK t " +
                "  where t.TRK_ID_C = :id ")
                .bind("id", id)
                .mapTo(Track.class)
                .first();
    }

    /**
     * Deletes a track.
     *
     * @param id Track ID
     */
    public void delete(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_TRACK t" +
                "  set t.TRK_DELETEDATE_D = :deleteDate" +
                "  where t.TRK_DELETEDATE_D is null and t.TRK_ID_C = :id ")
                .bind("id", id)
                .bind("deleteDate", new Timestamp(new Date().getTime()))
                .execute();
    }
}
