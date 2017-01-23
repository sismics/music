package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.UserAlbum;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.sql.Timestamp;
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
                .bind("createDate", new Timestamp(userAlbum.getCreateDate().getTime()))
                .execute();

        return userAlbum.getId();
    }
}
