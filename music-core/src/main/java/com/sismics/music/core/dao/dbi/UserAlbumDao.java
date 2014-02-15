package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.dao.dbi.mapper.UserAlbumMapper;
import com.sismics.music.core.model.dbi.UserAlbum;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.util.Date;
import java.util.UUID;

/**
 * User / album DAO.
 * 
 * @author jtremeaux
 */
public class UserAlbumDao {
    /**
     * Creates a new user / album.
     *
     * @param userAlbum User / album to create
     * @return User / album ID
     */
    public String create(UserAlbum userAlbum) {
        // Init user / album data
        userAlbum.setId(UUID.randomUUID().toString());
        userAlbum.setCreateDate(new Date());
        userAlbum.setScore(0);

        // Create user / album
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                " T_USER_ALBUM(USA_ID_C, USA_IDUSER_C, USA_IDALBUM_C, USA_CREATEDATE_D)" +
                " values(:id, :userId, :albumId, :createDate)")
                .bind("id", userAlbum.getId())
                .bind("userId", userAlbum.getUserId())
                .bind("albumId", userAlbum.getAlbumId())
                .bind("createDate", userAlbum.getCreateDate())
                .execute();

        return userAlbum.getId();
    }

    /**
     * Gets an active user / album.
     *
     * @param userId User ID
     * @param albumId Album ID
     * @return User / album
     */
    public UserAlbum getActiveUserAlbum(String userId, String albumId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new UserAlbumMapper().getJoinedColumns("ut") +
                "  from T_USER_ALBUM ua" +
                "  where ua.USA_DELETEDATE_D is null and ua.USA_IDUSER_C = :userId and ua.USA_IDALBUM_C = :albumId ")
                .bind("userId", userId)
                .bind("albumId", albumId)
                .bind("deleteDate", new Date())
                .mapTo(UserAlbum.class)
                .first();
    }
}
