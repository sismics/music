package com.sismics.music.core.dao.dbi;

import com.google.common.base.Joiner;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.dao.dbi.mapper.AlbumMapper;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.util.dbi.*;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.util.IntegerMapper;

import java.sql.Timestamp;
import java.util.*;

/**
 * Album DAO.
 * 
 * @author jtremeaux
 */
public class AlbumDao {
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
                " T_ALBUM(ALB_ID_C, ALB_IDDIRECTORY_C, ALB_IDARTIST_C, ALB_NAME_C, ALB_ALBUMART_C, ALB_CREATEDATE_D, ALB_UPDATEDATE_D, ALB_LOCATION_C)" +
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
    public Album update(Album album) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_ALBUM a set " +
                " a.ALB_IDDIRECTORY_C = :directoryId," +
                " a.ALB_IDARTIST_C = :artistId, " +
                " a.ALB_NAME_C = :name, " +
                " a.ALB_ALBUMART_C = :albumArt, " +
                " a.ALB_UPDATEDATE_D = :updateDate, " +
                " a.ALB_LOCATION_C = :location " +
                " where a.ALB_ID_C = :id and a.ALB_DELETEDATE_D is null")
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
        handle.createStatement("update T_ALBUM a set " +
                " a.ALB_UPDATEDATE_D = :updateDate " +
                " where a.ALB_ID_C = :id and a.ALB_DELETEDATE_D is null")
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
                "  from T_ALBUM a" +
                "  where lower(a.ALB_IDARTIST_C) = lower(:artistId) and lower(a.ALB_NAME_C) = lower(:name) and a.ALB_DELETEDATE_D is null")
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
        return handle.createQuery("select a.ALB_ID_C, a.ALB_IDDIRECTORY_C, a.ALB_IDARTIST_C, a.ALB_NAME_C, a.ALB_ALBUMART_C, a.ALB_UPDATEDATE_D, a.ALB_CREATEDATE_D, a.ALB_DELETEDATE_D, a.ALB_LOCATION_C" +
                "  from T_ALBUM a" +
                "  where a.ALB_IDARTIST_C = :artistId and a.ALB_DELETEDATE_D is null")
                .bind("artistId", artistId)
                .mapTo(Album.class)
                .list();
    }
    
    /**
     * Gets an active album by its ID.
     *
     * @param id Album ID
     * @return Album
     */
    public Album getActiveById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new AlbumMapper().getJoinedColumns("a") +
                "  from T_ALBUM a" +
                "  where a.ALB_ID_C = :id and a.ALB_DELETEDATE_D is null")
                .bind("id", id)
                .mapTo(Album.class)
                .first();
    }

    /**
     * Returns the total number of likes for this album.
     *
     * @param id Album ID
     * @return Number of likes
     */
    public Integer getFavoriteCountByAlbum(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select count(t.TRK_ID_C)" +
                "  from T_TRACK t" +
                "  where t.TRK_IDALBUM_C = :id and t.TRK_FAVORITE_B = :favorite and a.TRK_DELETEDATE_D is null")
                .bind("id", id)
                .map(IntegerMapper.FIRST)
                .first();
    }

    /**
     * Searches albums by criteria.
     *
     * @param criteria Search criteria
     * @return List of albums
     */
    public List<AlbumDto> findByCriteria(AlbumCriteria criteria) {
        QueryParam queryParam = getQueryParam(criteria);
        Query<Map<String, Object>> q = QueryUtil.getNativeQuery(queryParam);
        List<Object[]> l = q.map(ColumnIndexMapper.INSTANCE).list();
        return assembleResultList(l);
    }

    /**
     * Searches albums by criteria.
     *
     * @param paginatedList Paginated list (populated by side effects)
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     */
    public void findByCriteria(PaginatedList<AlbumDto> paginatedList, AlbumCriteria criteria, SortCriteria sortCriteria) {
        QueryParam queryParam = getQueryParam(criteria);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria, true);
        List<AlbumDto> albumDtoList = assembleResultList(l);
        paginatedList.setResultList(albumDtoList);
    }
    
    /**
     * Creates the query parameters from the criteria.
     *
     * @param criteria Search criteria
     * @return Query parameters
     */
    private QueryParam getQueryParam(AlbumCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select a.ALB_ID_C, a.ALB_NAME_C as c0,  a.ALB_ALBUMART_C, ar.ART_ID_C, ar.ART_NAME_C, a.ALB_UPDATEDATE_D as c1, ");
        if (criteria.getUserId() == null) {
            sb.append("sum(0)");
        } else {
            sb.append("sum(utr.UST_PLAYCOUNT_N) as c2");
        }
        sb.append(" from T_ALBUM a ");
        sb.append(" join T_ARTIST ar on(ar.ART_ID_C = a.ALB_IDARTIST_C) ");
        if (criteria.getUserId() != null) {
            sb.append(" left join T_TRACK tr on(tr.TRK_IDALBUM_C = a.ALB_ID_C) ");
            sb.append(" left join T_USER_TRACK utr on(tr.TRK_ID_C = utr.UST_IDTRACK_C) ");
        }

        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getId() != null) {
            criteriaList.add("a.ALB_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getDirectoryId() != null) {
            criteriaList.add("a.ALB_IDDIRECTORY_C = :directoryId");
            parameterMap.put("directoryId", criteria.getDirectoryId());
        }
        if (criteria.getArtistId() != null) {
            criteriaList.add("ar.ART_ID_C = :artistId");
            parameterMap.put("artistId", criteria.getArtistId());
        }
        if (criteria.getNameLike() != null) {
            criteriaList.add("(lower(a.ALB_NAME_C) like lower(:like) or lower(ar.ART_NAME_C) like lower(:like))");
            parameterMap.put("like", "%" + criteria.getNameLike() + "%");
        }
        criteriaList.add("ar.ART_DELETEDATE_D is null");
        criteriaList.add("a.ALB_DELETEDATE_D is null");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        sb.append(" group by a.ALB_ID_C ");

        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        return queryParam;
    }

    /**
     * Assemble the query results.
     *
     * @param resultList Query results as a table
     * @return Query results as a list of domain objects
     */
    private List<AlbumDto> assembleResultList(List<Object[]> resultList) {
        List<AlbumDto> albumDtoList = new ArrayList<AlbumDto>();
        for (Object[] o : resultList) {
            int i = 0;
            AlbumDto albumDto = new AlbumDto();
            albumDto.setId((String) o[i++]);
            albumDto.setName((String) o[i++]);
            albumDto.setAlbumArt((String) o[i++]);
            albumDto.setArtistId((String) o[i++]);
            albumDto.setArtistName((String) o[i++]);
            albumDto.setUpdateDate((Timestamp) o[i++]);
            Long playCount = (Long) o[i++];
            albumDto.setUserPlayCount(playCount == null ? 0 : playCount);
            albumDtoList.add(albumDto);
        }
        return albumDtoList;
    }

    /**
     * Deletes a album.
     * 
     * @param id Album ID
     */
    public void delete(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_ALBUM a" +
                "  set a.ALB_DELETEDATE_D = :deleteDate" +
                "  where a.ALB_ID_C = :id and a.ALB_DELETEDATE_D is null")
                .bind("id", id)
                .bind("deleteDate", new Timestamp(new Date().getTime()))
                .execute();
    }

    /**
     * Delete any album that don't have any tracks.
     */
    public void deleteEmptyAlbum() {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_ALBUM a set a.ALB_DELETEDATE_D = :deleteDate where a.ALB_ID_C IN (" +
                "  select al.ALB_ID_C from T_ALBUM al " +
                "    left join T_TRACK t on t.TRK_IDALBUM_C = al.ALB_ID_C and t.TRK_DELETEDATE_D is null " +
                "    where al.ALB_DELETEDATE_D is null " +
                "    group by al.ALB_ID_C " +
                "    having count(t.TRK_ID_C) = 0)")
                .bind("deleteDate", new Timestamp(new Date().getTime()))
                .execute();
    }
}
