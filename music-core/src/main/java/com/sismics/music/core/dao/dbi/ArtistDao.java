package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.dao.dbi.criteria.ArtistCriteria;
import com.sismics.music.core.dao.dbi.dto.ArtistDto;
import com.sismics.music.core.dao.dbi.mapper.ArtistDtoMapper;
import com.sismics.music.core.dao.dbi.mapper.ArtistMapper;
import com.sismics.music.core.model.dbi.Artist;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.BaseDao;
import com.sismics.util.dbi.filter.FilterCriteria;
import org.skife.jdbi.v2.Handle;

import java.sql.Timestamp;
import java.util.*;

/**
 * Artist DAO.
 * 
 * @author jtremeaux
 */
public class ArtistDao extends BaseDao<ArtistDto, ArtistCriteria> {
    @Override
    protected QueryParam getQueryParam(ArtistCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select a.ART_ID_C as id, a.ART_NAME_C as c0 ");
        sb.append(" from T_ARTIST a ");

        // Adds search criteria
        criteriaList.add("a.ART_DELETEDATE_D is null");
        if (criteria.getId() != null) {
            criteriaList.add("a.ART_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getNameLike() != null) {
            criteriaList.add("lower(a.ART_NAME_C) like lower(:nameLike)");
            parameterMap.put("nameLike", "%" + criteria.getNameLike() + "%");
        }

        return new QueryParam(sb.toString(), criteriaList, parameterMap, null, filterCriteria, new ArtistDtoMapper());
    }

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
                "  T_ARTIST (ART_ID_C, ART_NAME_C, ART_NAMECORRECTED_C, ART_CREATEDATE_D)" +
                "  values(:id, :name, :nameCorrected, :createDate)")
                .bind("id", artist.getId())
                .bind("name", artist.getName())
                .bind("nameCorrected", artist.getNameCorrected())
                .bind("createDate", new Timestamp(artist.getCreateDate().getTime()))
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
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_ARTIST a set " +
                " a.ART_NAME_C = :name," +
                " a.ART_NAMECORRECTED_C = :nameCorrected" +
                " where a.ART_ID_C = :id and a.ART_DELETEDATE_D is null")
                .bind("id", artist.getId())
                .bind("name", artist.getName())
                .bind("nameCorrected", artist.getNameCorrected())
                .execute();

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
                .bind("deleteDate", new Timestamp(new Date().getTime()))
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
                .bind("deleteDate", new Timestamp(new Date().getTime()))
                .execute();
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
