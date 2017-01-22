package com.sismics.music.core.dao.dbi;

import com.google.common.base.Joiner;
import com.sismics.music.core.dao.dbi.criteria.PlaylistCriteria;
import com.sismics.music.core.dao.dbi.dto.PlaylistDto;
import com.sismics.music.core.model.dbi.Playlist;
import com.sismics.music.core.util.dbi.*;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import java.util.*;

/**
 * Playlist DAO.
 * 
 * @author jtremeaux
 */
public class PlaylistDao {
    /**
     * Creates a new playlist.
     * 
     * @param playlist Playlist to create
     * @return Playlist ID
     */
    public String create(Playlist playlist) {
        playlist.setId(UUID.randomUUID().toString());

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                "  T_PLAYLIST(PLL_ID_C, PLL_IDUSER_C, PLL_NAME_C)" +
                "  values(:id, :userId, :name)")
                .bind("id", playlist.getId())
                .bind("userId", playlist.getUserId())
                .bind("name", playlist.getName())
                .execute();

        return playlist.getId();
    }

    /**
     * Gets a playlist by user ID.
     *
     * @param userId User ID
     * @return Playlist
     */
    public PlaylistDto getDefaultPlaylistByUserId(String userId) {
        return findFirstByCriteria(new PlaylistCriteria().setUserId(userId));
    }

    /**
     * Creates the query parameters from the criteria.
     *
     * @param criteria Search criteria
     * @return Query parameters
     */
    private QueryParam getQueryParam(PlaylistCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select p.PLL_ID_C, p.PLL_NAME_C as c0,")
                .append("  p.PLL_IDUSER_C ")
                .append("  from T_PLAYLIST p ");
        
        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.getId() != null) {
            criteriaList.add("p.PLL_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getUserId() != null) {
            criteriaList.add("p.PLL_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        if (criteria.getDefaultPlaylist() != null) {
            if (criteria.getDefaultPlaylist()) {
                criteriaList.add("p.PLL_NAME_C is null");
            } else {
                criteriaList.add("p.PLL_NAME_C is not null");
            }
        }
        if (criteria.getNameLike() != null) {
            criteriaList.add("lower(p.PLL_NAME_C) like lower(:nameLike)");
            parameterMap.put("nameLike", "%" + criteria.getNameLike() + "%");
        }

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        return new QueryParam(sb.toString(), parameterMap);
    }

    /**
     * Searches playlists by criteria.
     *
     * @param criteria Search criteria
     * @return List of playlists
     */
    public List<PlaylistDto> findByCriteria(PlaylistCriteria criteria) {
        QueryParam queryParam = getQueryParam(criteria);
        Query<Map<String, Object>> q = QueryUtil.getNativeQuery(queryParam);
        List<Object[]> l = q.map(ColumnIndexMapper.INSTANCE).list();
        return assembleResultList(l);
    }

    /**
     * Searches playlists by criteria.
     *
     * @param paginatedList Paginated list (populated by side effects)
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     */
    public void findByCriteria(PaginatedList<PlaylistDto> paginatedList, PlaylistCriteria criteria, SortCriteria sortCriteria) {
        QueryParam queryParam = getQueryParam(criteria);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria, true);
        List<PlaylistDto> playlistDtoList = assembleResultList(l);
        paginatedList.setResultList(playlistDtoList);
    }

    /**
     * Searches playlists by criteria.
     *
     * @param criteria Search criteria
     * @return List of playlists
     */
    public PlaylistDto findFirstByCriteria(PlaylistCriteria criteria) {
        List<PlaylistDto> list = findByCriteria(criteria);
        if (list != null && !list.isEmpty()) {
            return list.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Assemble the query results.
     *
     * @param resultList Query results as a table
     * @return Query results as a list of domain objects
     */
    private List<PlaylistDto> assembleResultList(List<Object[]> resultList) {
        List<PlaylistDto> playlistDtoList = new ArrayList<PlaylistDto>();
        for (Object[] o : resultList) {
            int i = 0;
            PlaylistDto playlistDto = new PlaylistDto();
            playlistDto.setId((String) o[i++]);
            playlistDto.setName((String) o[i++]);
            playlistDto.setUserId((String) o[i]);
            playlistDtoList.add(playlistDto);
        }
        return playlistDtoList;
    }
}
