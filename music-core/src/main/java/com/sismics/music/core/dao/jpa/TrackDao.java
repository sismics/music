package com.sismics.music.core.dao.jpa;

import com.google.common.base.Joiner;
import com.sismics.music.core.dao.jpa.criteria.TrackCriteria;
import com.sismics.music.core.dao.jpa.dto.TrackDto;
import com.sismics.music.core.model.jpa.Track;
import com.sismics.music.core.util.jpa.PaginatedList;
import com.sismics.music.core.util.jpa.PaginatedLists;
import com.sismics.music.core.util.jpa.QueryParam;
import com.sismics.music.core.util.jpa.QueryUtil;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
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

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(track);
        
        return track.getId();
    }
    
    /**
     * Updates a track.
     * 
     * @param track Track to update
     * @return Updated track
     */
    public Track update(Track track) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the track
        Query q = em.createQuery("select d from Track d where d.id = :id and d.deleteDate is null");
        q.setParameter("id", track.getId());
        Track trackFromDb = (Track) q.getSingleResult();

        // Update the track

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
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select t from Track t, Album a where t.albumId = a.id and t.fileName = :fileName and t.deleteDate is null and a.deleteDate is null and a.directoryId = :directoryId");
            q.setParameter("directoryId", directoryId);
            q.setParameter("fileName", fileName);
            return (Track) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active track by its trackname.
     *
     * @param id Track ID
     * @return Track
     */
    public Track getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select t from Track t where t.id = :id and t.deleteDate is null");
            q.setParameter("id", id);
            return (Track) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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
     * Creates the query parameters from the criteria.
     *
     * @param criteria Search criteria
     * @return Query parameters
     */
    private QueryParam getQueryParam(TrackCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select t.TRK_ID_C, t.TRK_FILENAME_C, t.TRK_TITLE_C, t.TRK_YEAR_N, t.TRK_LENGTH_N, t.TRK_BITRATE_N, t.TRK_VBR_B, t.TRK_FORMAT_C, ");
        sb.append(" a.ART_ID_C, a.ART_NAME_C, t.TRK_IDALBUM_C, alb.ALB_NAME_C ");
        sb.append(" from T_TRACK t ");
        sb.append(" join T_ARTIST a ON(a.ART_ID_C = t.TRK_IDARTIST_C) ");
        sb.append(" join T_ALBUM alb ON(t.TRK_IDALBUM_C = alb.ALB_ID_C) ");
        if (criteria.getUserId() != null) {
            sb.append(" join T_PLAYLIST_TRACK pt ON(pt.PLT_IDTRACK_C = t.TRK_ID_C) ");
            sb.append(" join T_PLAYLIST p ON(p.PLL_ID_C = pt.PLT_IDPLAYLIST_C) ");
        }

        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getAlbumId() != null) {
            criteriaList.add("t.TRK_IDALBUM_C = :albumId");
            parameterMap.put("albumId", criteria.getAlbumId());
        }
        if (criteria.getTitleLike() != null) {
            criteriaList.add("lower(t.TRK_TITLE_C) like lower(:titleLike)");
            parameterMap.put("titleLike", "%" + criteria.getTitleLike() + "%");
        }
        if (criteria.getUserId() != null) {
            criteriaList.add("p.PLL_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        criteriaList.add("t.TRK_DELETEDATE_D is null");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        if (criteria.getUserId() != null) {
            sb.append(" order by pt.PLT_ORDER_N asc");
        } else {
            sb.append(" order by t.TRK_TITLE_C asc"); //TODO add order column
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
            trackDto.setLength((Integer) o[i++]);
            trackDto.setBitrate((Integer) o[i++]);
            trackDto.setVbr((Boolean) o[i++]);
            trackDto.setFormat((String) o[i++]);
            trackDto.setArtistId((String) o[i++]);
            trackDto.setArtistName((String) o[i++]);
            trackDto.setAlbumId((String) o[i++]);
            trackDto.setAlbumName((String) o[i++]);
            trackDtoList.add(trackDto);
        }
        return trackDtoList;
    }

    /**
     * Searches tracks by criteria.
     *
     * @param criteria Search criteria
     * @return List of tracks
     */
    @SuppressWarnings("unchecked")
    public List<TrackDto> findByCriteria(TrackCriteria criteria) {
        QueryParam queryParam = getQueryParam(criteria);
        org.skife.jdbi.v2.Query q = QueryUtil.getNativeQuery(queryParam);
        List<Object[]> l = q.list();
        return assembleResultList(l);
    }

    /**
     * Deletes all tracks from an album.
     * 
     * @param albumId Album ID
     */
    public void deleteFromAlbum(String albumId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the track
        Query q = em.createNativeQuery("update T_TRACK set TRK_DELETEDATE_D = :deleteDate WHERE TRK_DELETEDATE_D is null and TRK_IDALBUM_C = :albumId ");
        q.setParameter("albumId", albumId);
        q.setParameter("deleteDate", new Date());
        q.executeUpdate();
    }

    /**
     * Deletes a track.
     *
     * @param id Track ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the track
        Query q = em.createQuery("select t from Track t where t.id = :id and t.deleteDate is null");
        q.setParameter("id", id);
        Track trackFromDb = (Track) q.getSingleResult();

        // Delete the track
        Date dateNow = new Date();
        trackFromDb.setDeleteDate(dateNow);
    }
}
