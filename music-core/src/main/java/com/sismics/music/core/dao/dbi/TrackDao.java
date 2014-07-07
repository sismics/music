package com.sismics.music.core.dao.dbi;

import com.google.common.base.Joiner;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.dao.dbi.mapper.TrackMapper;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.util.dbi.*;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import java.util.*;

/**
 * Track DAO.
 * 
 * @author jtremeaux
 */
public class TrackDao {
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
                "  T_TRACK(TRK_ID_C, TRK_IDALBUM_C, TRK_IDARTIST_C, TRK_FILENAME_C, TRK_TITLE_C, TRK_YEAR_N, TRK_GENRE_C, TRK_LENGTH_N, TRK_BITRATE_N, TRK_ORDER_N, TRK_VBR_B, TRK_FORMAT_C, TRK_CREATEDATE_D)" +
                "  values(:id, :albumId, :artistId, :fileName, :title, :year, :genre, :length, :bitrate, :order, :vbr, :format, :createDate)")
                .bind("id", track.getId())
                .bind("albumId", track.getAlbumId())
                .bind("artistId", track.getArtistId())
                .bind("fileName", track.getFileName())
                .bind("title", track.getTitle())
                .bind("year", track.getYear())
                .bind("genre", track.getGenre())
                .bind("length", track.getLength())
                .bind("bitrate", track.getBitrate())
                .bind("order", track.getOrder())
                .bind("vbr", track.isVbr())
                .bind("format", track.getFormat())
                .bind("createDate", track.getCreateDate())
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
                .bind("year", track.getYear())
                .bind("genre", track.getGenre())
                .bind("length", track.getLength())
                .bind("bitrate", track.getBitrate())
                .bind("order", track.getOrder())
                .bind("vbr", track.isVbr())
                .bind("format", track.getFormat())
                .bind("createDate", track.getCreateDate())
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
     * Searches tracks by criteria.
     *
     * @param criteria Search criteria
     * @param paginatedList Paginated list (populated by side effects)
     */
    public void findByCriteria(TrackCriteria criteria, PaginatedList<TrackDto> paginatedList) {
        QueryParam queryParam = getQueryParam(criteria);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, false);
        List<TrackDto> trackDtoList = assembleResultList(l);
        paginatedList.setResultList(trackDtoList);
    }

    /**
     * Searches tracks by criteria.
     *
     * @param criteria Search criteria
     * @return List of tracks
     */
    public List<TrackDto> findByCriteria(TrackCriteria criteria) {
        QueryParam queryParam = getQueryParam(criteria);
        Query<Map<String, Object>> q = QueryUtil.getNativeQuery(queryParam);
        List<Object[]> l = q.map(ColumnIndexMapper.INSTANCE).list();
        return assembleResultList(l);
    }

    /**
     * Creates the query parameters from the criteria.
     *
     * @param criteria Search criteria
     * @return Query parameters
     */
    private QueryParam getQueryParam(TrackCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select t.TRK_ID_C, t.TRK_FILENAME_C, t.TRK_TITLE_C, t.TRK_YEAR_N, t.TRK_GENRE_C, t.TRK_LENGTH_N, t.TRK_BITRATE_N, t.TRK_ORDER_N, t.TRK_VBR_B, t.TRK_FORMAT_C,");
        if (criteria.getUserId() != null) {
            sb.append(" ut.UST_PLAYCOUNT_N, ut.UST_LIKE_B, ");
        } else {
            sb.append(" 0, false, ");
        }
        sb.append(" a.ART_ID_C, a.ART_NAME_C, t.TRK_IDALBUM_C, alb.ALB_NAME_C, alb.ALB_ALBUMART_C ");
        sb.append(" from T_TRACK t ");
        sb.append(" join T_ARTIST a ON(a.ART_ID_C = t.TRK_IDARTIST_C and ART_DELETEDATE_D is null) ");
        sb.append(" join T_ALBUM alb ON(t.TRK_IDALBUM_C = alb.ALB_ID_C and alb.ALB_DELETEDATE_D is null) ");
        if (criteria.getUserId() != null) {
            sb.append(" left join T_USER_TRACK ut ON(ut.UST_IDTRACK_C = t.TRK_ID_C and ut.UST_IDUSER_C = :userId and ut.UST_DELETEDATE_D is null) ");
        }
        if (criteria.getPlaylistId() != null) {
            sb.append(" join T_PLAYLIST_TRACK pt ON(pt.PLT_IDTRACK_C = t.TRK_ID_C) ");
            sb.append(" join T_PLAYLIST p ON(p.PLL_ID_C = pt.PLT_IDPLAYLIST_C and p.PLL_IDUSER_C = :userId) ");
        }

        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getAlbumId() != null) {
            criteriaList.add("t.TRK_IDALBUM_C = :albumId");
            parameterMap.put("albumId", criteria.getAlbumId());
        }
        if (criteria.getArtistName() != null) {
            criteriaList.add("a.ART_NAME_C = :artistName");
            parameterMap.put("artistName", criteria.getArtistName());
        }
        if (criteria.getTitle() != null) {
            criteriaList.add("lower(t.TRK_TITLE_C) = lower(:title)");
            parameterMap.put("title", criteria.getTitle());
        }
        if (criteria.getArtistName() != null) {
            criteriaList.add("a.ART_NAME_C = :artistName");
            parameterMap.put("artistName", criteria.getArtistName());
        }
        if (criteria.getTitle() != null) {
            criteriaList.add("lower(t.TRK_TITLE_C) = lower(:title)");
            parameterMap.put("title", criteria.getTitle());
        }
        if (criteria.getTitleLike() != null) {
            criteriaList.add("lower(t.TRK_TITLE_C) like lower(:titleLike)");
            parameterMap.put("titleLike", "%" + criteria.getTitleLike() + "%");
        }
        if (criteria.getUserId() != null) {
            parameterMap.put("userId", criteria.getUserId());
        }
        criteriaList.add("t.TRK_DELETEDATE_D is null");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        if (criteria.getPlaylistId() != null) {
            sb.append(" order by pt.PLT_ORDER_N asc");
        } else {
            sb.append(" order by t.TRK_ORDER_N, t.TRK_TITLE_C asc");
        }

        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        return queryParam;
    }

    /**
     * Assemble the query results.
     *
     * @param l Query results as a table
     * @return Query results as a list of domain objects
     */
    private List<TrackDto> assembleResultList(List<Object[]> l) {
        List<TrackDto> trackDtoList = new ArrayList<TrackDto>();
        for (Object[] o : l) {
            int i = 0;
            TrackDto trackDto = new TrackDto();
            trackDto.setId((String) o[i++]);
            trackDto.setFileName((String) o[i++]);
            trackDto.setTitle((String) o[i++]);
            trackDto.setYear((Integer) o[i++]);
            trackDto.setGenre((String) o[i++]);
            trackDto.setLength((Integer) o[i++]);
            trackDto.setBitrate((Integer) o[i++]);
            trackDto.setOrder((Integer) o[i++]);
            trackDto.setVbr((Boolean) o[i++]);
            trackDto.setFormat((String) o[i++]);
            Integer trackCount = (Integer) o[i++];
            if (trackCount == null) {
                trackCount = 0;
            }
            trackDto.setUserTrackPlayCount(trackCount);
            Boolean favorite = (Boolean) o[i++];
            if (favorite != null) {
                trackDto.setUserTrackLike(favorite);
            }
            trackDto.setArtistId((String) o[i++]);
            trackDto.setArtistName((String) o[i++]);
            trackDto.setAlbumId((String) o[i++]);
            trackDto.setAlbumName((String) o[i++]);
            trackDto.setAlbumArt((String) o[i++]);
            trackDtoList.add(trackDto);
        }
        return trackDtoList;
    }

    /**
     * Deletes all tracks from an album.
     * 
     * @param albumId Album ID
     */
    public void deleteFromAlbum(String albumId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_TRACK t" +
                "  set t.TRK_DELETEDATE_D = :deleteDate" +
                "  where WHERE t.TRK_DELETEDATE_D is null and t.TRK_IDALBUM_C = :albumId ")
                .bind("albumId", albumId)
                .bind("deleteDate", new Date())
                .execute();
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
                "  where WHERE t.TRK_DELETEDATE_D is null and t.TRK_ID_C = :id ")
                .bind("id", id)
                .bind("deleteDate", new Date())
                .execute();
    }
}
