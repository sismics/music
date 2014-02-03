package com.sismics.music.core.dao.jpa;

import com.google.common.base.Joiner;
import com.sismics.music.core.dao.jpa.criteria.AlbumCriteria;
import com.sismics.music.core.dao.jpa.dto.AlbumDto;
import com.sismics.music.core.model.jpa.Album;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
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
        album.setCreateDate(new Date());

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(album);
        
        return album.getId();
    }
    
    /**
     * Updates a album.
     * 
     * @param album Album to update
     * @return Updated album
     */
    public Album update(Album album) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the album
        Query q = em.createQuery("select a from Album a where a.id = :id and a.deleteDate is null");
        q.setParameter("id", album.getId());
        Album albumFromDb = (Album) q.getSingleResult();

        // Update the album

        return album;
    }
    
    /**
     * Gets an active album by its name.
     * 
     * @param name Album name
     * @return Album
     */
    public Album getActiveByArtistIdAndName(String artistId, String name) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select a from Album a where a.artistId = :artistId and a.name = :name and a.deleteDate is null");
            q.setParameter("artistId", artistId);
            q.setParameter("name", name);
            return (Album) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets an active album by its albumname.
     *
     * @param id Album ID
     * @return Album
     */
    public Album getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select a from Album a where a.id = :id and a.deleteDate is null");
            q.setParameter("id", id);
            return (Album) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Searches albums by criteria.
     *
     * @param criteria Search criteria
     * @return List of albums
     */
    @SuppressWarnings("unchecked")
    public List<AlbumDto> findByCriteria(AlbumCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select a.ALB_ID_C, a.ALB_NAME_C,  a.ALB_ALBUMART_C, ar.ART_ID_C, ar.ART_NAME_C ");
        sb.append(" from T_ALBUM a ");
        sb.append(" join T_ARTIST ar on(ar.ART_ID_C = a.ALB_IDARTIST_C) ");

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
        criteriaList.add("ar.ART_DELETEDATE_D is null");
        criteriaList.add("a.ALB_DELETEDATE_D is null");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        sb.append(" order by ar.ART_NAME_C, a.ALB_NAME_C asc");

        // Search
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> resultList = q.getResultList();

        // Assemble results
        List<AlbumDto> albumDtoList = new ArrayList<AlbumDto>();
        for (Object[] o : resultList) {
            int i = 0;
            AlbumDto albumDto = new AlbumDto();
            albumDto.setId((String) o[i++]);
            albumDto.setName((String) o[i++]);
            albumDto.setAlbumArt((String) o[i++]);
            albumDto.setArtistId((String) o[i++]);
            albumDto.setArtistName((String) o[i++]);
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
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the album
        Query q = em.createQuery("select a from Album a where a.id = :id and a.deleteDate is null");
        q.setParameter("id", id);
        Album albumFromDb = (Album) q.getSingleResult();
        
        // Delete the album
        Date dateNow = new Date();
        albumFromDb.setDeleteDate(dateNow);

        // Delete linked tracks
        TrackDao trackDao = new TrackDao();
        trackDao.deleteFromAlbum(id);
    }
}
