package com.sismics.music.core.dao.dbi;

import com.google.common.base.Joiner;
import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
import com.sismics.music.core.dao.dbi.dto.AlbumDto;
import com.sismics.music.core.model.dbi.Album;
import com.sismics.music.core.util.dbi.ColumnIndexMapper;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.music.core.util.dbi.QueryUtil;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

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

        Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                " T_ALBUM(ALB_ID_C, ALB_IDDIRECTORY_C, ALB_IDARTIST_C, ALB_NAME_C, ALB_ALBUMART_C, ALB_CREATEDATE_D)" +
                " values(:id, :directoryId, :artistId, :name, :albumArt, :createDate)")
                .bind("id", album.getId())
                .bind("directoryId", album.getDirectoryId())
                .bind("artistId", album.getArtistId())
                .bind("name", album.getName())
                .bind("albumArt", album.getAlbumArt())
                .bind("createDate", album.getCreateDate())
                .execute();

        return album.getId();
    }
    
    /**
     * Updates a album.
     * 
     * @param album Album to update
     * @return Updated album
     */
    public Album update(Album album) {
//        EntityManager em = ThreadLocalContext.get().getEntityManager();
//
//        // Get the album
//        Query q = em.createQuery("select a from Album a where a.id = :id and a.deleteDate is null");
//        q.setParameter("id", album.getId());
//        Album albumFromDb = (Album) q.getSingleResult();

        // TODO Update the album

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
        return handle.createQuery("select a.ALB_ID_C, a.ALB_IDDIRECTORY_C, a.ALB_IDARTIST_C, a.ALB_NAME_C, a.ALB_ALBUMART_C, a.ALB_CREATEDATE_D, a.ALB_DELETEDATE_D" +
                "  from T_ALBUM a" +
                "  where a.ALB_IDARTIST_C = :artistId and a.ALB_NAME_C = :name and a.ALB_DELETEDATE_D is null")
                .bind("artistId", artistId)
                .bind("name", name)
                .mapTo(Album.class)
                .first();
    }
    
    /**
     * Gets an active album by its albumname.
     *
     * @param id Album ID
     * @return Album
     */
    public Album getActiveById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select a.ALB_ID_C, a.ALB_IDDIRECTORY_C, a.ALB_IDARTIST_C, a.ALB_NAME_C, a.ALB_ALBUMART_C, a.ALB_CREATEDATE_D, a.ALB_DELETEDATE_D" +
                "  from T_ALBUM a" +
                "  where a.ALB_ID_C = :id and a.ALB_DELETEDATE_D is null")
                .bind("id", id)
                .mapTo(Album.class)
                .first();
    }

    /**
     * Searches albums by criteria.
     *
     * @param criteria Search criteria
     * @return List of albums
     */
    @SuppressWarnings("unchecked")
    public List<AlbumDto> findByCriteria(AlbumCriteria criteria) {
        QueryParam queryParam = getQueryParam(criteria);
        Query q = QueryUtil.getNativeQuery(queryParam);
        List<Object[]> l = q.map(ColumnIndexMapper.INSTANCE).list();
        return assembleResultList(l);
    }

    /**
     * Creates the query parameters from the criteria.
     *
     * @param criteria Search criteria
     * @return Query parameters
     */
    private QueryParam getQueryParam(AlbumCriteria criteria) {
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
                .bind("deleteDate", new Date())
                .execute();
    }
}
