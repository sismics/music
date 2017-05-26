package com.sismics.music.core.dao.dbi;

import com.google.common.collect.Lists;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.dao.dbi.mapper.AlbumDtoMapper;
import com.sismics.music.core.dao.dbi.mapper.AlbumMapper;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.BaseDao;
import com.sismics.util.dbi.filter.FilterCriteria;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

import java.sql.Timestamp;
import java.util.*;

/**
 * Album DAO.
 * 
 * @author jtremeaux
 */
public class AlbumDao extends BaseDao<AlbumDto, AlbumCriteria> {
    @Override
    public QueryParam getQueryParam(AlbumCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<>();
        Map<String, Object> parameterMap = new HashMap<>();

        StringBuilder sb = new StringBuilder("select a.id as id, a.name as c0, a.albumart as albumArt, a.artist_id as artistId, ar.name as artistName, a.updatedate as c1, ");
        if (criteria.getUserId() == null) {
            sb.append("sum(0) as c2");
        } else {
            sb.append("sum(utr.playcount) as c2");
        }
        sb.append(" from t_album a ");
        sb.append(" join t_artist ar on(ar.id = a.artist_id) ");
        if (criteria.getUserId() != null) {
            sb.append(" left join t_track tr on(tr.album_id = a.id) ");
            sb.append(" left join t_user_track utr on(tr.id = utr.track_id) ");
        }

        // Adds search criteria
        criteriaList.add("ar.deletedate is null");
        criteriaList.add("a.deletedate is null");
        if (criteria.getId() != null) {
            criteriaList.add("a.id = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getDirectoryId() != null) {
            criteriaList.add("a.directory_id = :directoryId");
            parameterMap.put("directoryId", criteria.getDirectoryId());
        }
        if (criteria.getArtistId() != null) {
            criteriaList.add("ar.id = :artistId");
            parameterMap.put("artistId", criteria.getArtistId());
        }
        if (criteria.getNameLike() != null) {
            criteriaList.add("(lower(a.name) like lower(:like) or lower(ar.name) like lower(:like))");
            parameterMap.put("like", "%" + criteria.getNameLike() + "%");
        }

        return new QueryParam(sb.toString(), criteriaList, parameterMap, null, filterCriteria, Lists.newArrayList("a.id"), new AlbumDtoMapper());
    }

    /**
     * Creates a new album.
     * 
     * @param album Album to create
     * @return Album ID
     */
    public String create(Album album) {
        album.setId(UUID.randomUUID().toString());
        final Date now = new Date();
        if (album.getCreateDate() == null) {
            album.setCreateDate(now);
        }
        if (album.getUpdateDate() == null) {
            album.setUpdateDate(now);
        }

        Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                " t_album(id, directory_id, artist_id, name, albumart, createdate, updatedate, location)" +
                " values(:id, :directoryId, :artistId, :name, :albumArt, :createDate, :updateDate, :location)")
                .bind("id", album.getId())
                .bind("directoryId", album.getDirectoryId())
                .bind("artistId", album.getArtistId())
                .bind("name", album.getName())
                .bind("albumArt", album.getAlbumArt())
                .bind("updateDate", new Timestamp(album.getUpdateDate().getTime()))
                .bind("createDate", new Timestamp(album.getCreateDate().getTime()))
                .bind("location", album.getLocation())
                .execute();

        return album.getId();
    }
    
    /**
     * Updates an album.
     * 
     * @param album Album to update
     * @return Updated album
     */
    public static Album update(Album album) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_album a set " +
                " a.directory_id = :directoryId," +
                " a.artist_id = :artistId, " +
                " a.name = :name, " +
                " a.albumart = :albumArt, " +
                " a.updatedate = :updateDate, " +
                " a.location = :location " +
                " where a.id = :id and a.deletedate is null")
                .bind("id", album.getId())
                .bind("name", album.getName())
                .bind("directoryId", album.getDirectoryId())
                .bind("artistId", album.getArtistId())
                .bind("albumArt", album.getAlbumArt())
                .bind("updateDate", new Timestamp(album.getUpdateDate().getTime()))
                .bind("location", album.getLocation())
                .execute();

        return album;
    }

    /**
     * Updates an album date.
     *
     * @param album Album to update
     * @return Updated album
     */
    public Album updateAlbumDate(Album album) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_album a set " +
                " a.updatedate = :updateDate " +
                " where a.id = :id and a.deletedate is null")
                .bind("id", album.getId())
                .bind("updateDate", new Timestamp(album.getUpdateDate().getTime()))
                .execute();

        return album;
    }

    /**
     * Gets an active album by its name.
     * 
     * @param name Album name
     * @return Album
     */
    public Album getActiveByArtistIdAndName(String artistId, String name) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new AlbumMapper().getJoinedColumns("a") +
                "  from t_album a" +
                "  where lower(a.artist_id) = lower(:artistId) and lower(a.name) = lower(:name) and a.deletedate is null")
                .bind("artistId", artistId)
                .bind("name", name)
                .mapTo(Album.class)
                .first();
    }
    
    /**
     * Gets active albums by artist ID.
     * 
     * @param artistId Artist ID
     * @return List of albums
     */
    public List<Album> getActiveByArtistId(String artistId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select a.id, a.directory_id, a.artist_id, a.name, a.albumart, a.updatedate, a.createdate, a.deletedate, a.location" +
                "  from t_album a" +
                "  where a.artist_id = :artistId and a.deletedate is null")
                .bind("artistId", artistId)
                .mapTo(Album.class)
                .list();
    }

    /**
     * Returns the total number of likes for this album.
     *
     * @param id Album ID
     * @return Number of likes
     */
    public Integer getFavoriteCountByAlbum(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select count(t.id)" +
                "  from t_track t" +
                "  where t.album_id = :id and t.favorite = :favorite and a.deletedate is null")
                .bind("id", id)
                .map(IntegerMapper.FIRST)
                .first();
    }

    /**
     * Deletes a album.
     * 
     * @param id Album ID
     */
    public void delete(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_album a" +
                "  set a.deletedate = :deleteDate" +
                "  where a.id = :id and a.deletedate is null")
                .bind("id", id)
                .bind("deleteDate", new Timestamp(new Date().getTime()))
                .execute();
    }

    /**
     * Delete any album that don't have any tracks.
     */
    public void deleteEmptyAlbum() {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_album a set a.deletedate = :deleteDate where a.id in (" +
                "  select al.id from t_album al " +
                "    left join t_track t on t.album_id = al.id and t.deletedate is null " +
                "    where al.deletedate is null " +
                "    group by al.id " +
                "    having count(t.id) = 0)")
                .bind("deleteDate", new Timestamp(new Date().getTime()))
                .execute();
    }
}
