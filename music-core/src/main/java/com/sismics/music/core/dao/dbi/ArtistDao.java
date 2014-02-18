package com.sismics.music.core.dao.dbi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import com.google.common.base.Joiner;
import com.sismics.music.core.dao.dbi.criteria.ArtistCriteria;
import com.sismics.music.core.dao.dbi.dto.ArtistDto;
import com.sismics.music.core.dao.dbi.mapper.ArtistMapper;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.util.dbi.ColumnIndexMapper;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.music.core.util.dbi.QueryUtil;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Artist DAO.
 * 
 * @author jtremeaux
 */
public class ArtistDao {
    /**
     * Creates a new artist.
     * 
     * @param artist Artist to create
     * @return Artist ID
     */
    public String create(Artist artist) {
        artist.setId(UUID.randomUUID().toString());
        artist.setCreateDate(new Date());

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                "  T_ARTIST (ART_ID_C, ART_NAME_C, ART_CREATEDATE_D)" +
                "  values(:id, :name, :createDate)")
                .bind("id", artist.getId())
                .bind("name", artist.getName())
                .bind("createDate", artist.getCreateDate())
                .execute();

        return artist.getId();
    }
    
    /**
     * Updates a artist.
     * 
     * @param artist Artist to update
     * @return Updated artist
     */
    public Artist update(Artist artist) {

        // TODO Update the artist

        return artist;
    }
    
    /**
     * Gets an active artist by its name.
     * 
     * @param name Artist name
     * @return Artist
     */
    public Artist getActiveByName(String name) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new ArtistMapper().getJoinedColumns("a") +
                "  from T_ARTIST a" +
                "  where lower(a.ART_NAME_C) = lower(:name) and a.ART_DELETEDATE_D is null")
                .bind("name", name)
                .mapTo(Artist.class)
                .first();
    }
    
    /**
     * Gets an active artist by its artistname.
     *
     * @param id Artist ID
     * @return Artist
     */
    public Artist getActiveById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new ArtistMapper().getJoinedColumns("a") +
                "  from T_ARTIST a" +
                "  where a.ART_ID_C = :id and a.ART_DELETEDATE_D is null")
                .bind("id", id)
                .mapTo(Artist.class)
                .first();
    }

    /**
     * Deletes a artist.
     * 
     * @param id Artist ID
     */
    public void delete(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_ARTIST a" +
                "  set a.ART_DELETEDATE_D = :deleteDate" +
                "  where a.ART_ID_C = :id and a.ART_DELETEDATE_D is null")
                .bind("id", id)
                .bind("deleteDate", new Date())
                .execute();
    }

    /**
     * Delete any artist that don't have any album or track.
     */
    public void deleteEmptyArtist() {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_ARTIST a set a.ART_DELETEDATE_D = :deleteDate where a.ART_ID_C NOT IN (" +
                "  select al.ALB_IDARTIST_C from T_ALBUM al " +
                "    where al.ALB_DELETEDATE_D is null " +
                "    group by al.ALB_IDARTIST_C" +
                " union " +
                "  select t.TRK_IDARTIST_C from T_TRACK t " +
                "    where t.TRK_DELETEDATE_D is null " +
                "    group by t.TRK_IDARTIST_C)")
                .bind("deleteDate", new Date())
                .execute();
    }
    
    /**
     * Searches artists by criteria.
     *
     * @param criteria Search criteria
     * @return List of artists
     */
    public List<ArtistDto> findByCriteria(ArtistCriteria criteria) {
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
    private QueryParam getQueryParam(ArtistCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select a.ART_ID_C, a.ART_NAME_C ");
        sb.append(" from T_ARTIST a ");

        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getId() != null) {
            criteriaList.add("a.ART_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getNameLike() != null) {
            criteriaList.add("lower(a.ART_NAME_C) like lower(:nameLike)");
            parameterMap.put("nameLike", "%" + criteria.getNameLike() + "%");
        }
        criteriaList.add("a.ART_DELETEDATE_D is null");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        sb.append(" order by a.ART_NAME_C asc");


        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        return queryParam;
    }

    /**
     * Assemble the query results.
     *
     * @param resultList Query results as a table
     * @return Query results as a list of domain objects
     */
    private List<ArtistDto> assembleResultList(List<Object[]> resultList) {
        List<ArtistDto> artistDtoList = new ArrayList<ArtistDto>();
        for (Object[] o : resultList) {
            int i = 0;
            ArtistDto artistDto = new ArtistDto();
            artistDto.setId((String) o[i++]);
            artistDto.setName((String) o[i++]);
            artistDtoList.add(artistDto);
        }
        return artistDtoList;
    }
}
