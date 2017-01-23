package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.dao.dbi.criteria.PlaylistCriteria;
import com.sismics.music.core.dao.dbi.dto.PlaylistDto;
import com.sismics.music.core.dao.dbi.mapper.PlaylistMapper;
import com.sismics.music.core.model.dbi.Playlist;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.BaseDao;
import com.sismics.util.dbi.filter.FilterCriteria;
import org.skife.jdbi.v2.Handle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Playlist DAO.
 * 
 * @author jtremeaux
 */
public class PlaylistDao extends BaseDao<PlaylistDto, PlaylistCriteria> {
    @Override
    protected QueryParam getQueryParam(PlaylistCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select p.PLL_ID_C as id, p.PLL_NAME_C as c0,")
                .append("  p.PLL_IDUSER_C as userId ")
                .append("  from T_PLAYLIST p ");

        // Adds search criteria
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

        return new QueryParam(sb.toString(), criteriaList, parameterMap, null, filterCriteria, new PlaylistMapper());
    }

    /**
     * Creates a new playlist.
     * 
     * @param playlist Playlist to create
     * @return Playlist ID
     */
    public String create(Playlist playlist) {
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
     * Update a playlist.
     *
     * @param playlist Playlist to update
     */
    public void update(Playlist playlist) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_PLAYLIST" +
                "  set PLL_NAME_C = :name" +
                "  where PLL_ID_C = :id")
                .bind("name", playlist.getName())
                .bind("id", playlist.getId())
                .execute();
    }

    /**
     * Delete a playlist.
     *
     * @param playlist Playlist to delete
     */
    public void delete(Playlist playlist) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("delete from " +
                "  T_PLAYLIST_TRACK" +
                "  where PLT_IDPLAYLIST_C = :playlistId")
                .bind("playlistId", playlist.getId())
                .execute();
        handle.createStatement("delete from " +
                "  T_PLAYLIST" +
                "  where PLL_ID_C = :id")
                .bind("id", playlist.getId())
                .execute();
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
