package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.dao.dbi.mapper.UserTrackMapper;
import com.sismics.music.core.model.dbi.UserTrack;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.util.Date;
import java.util.UUID;

/**
 * User / track DAO.
 * 
 * @author jtremeaux
 */
public class UserTrackDao {
    /**
     * Creates a new user / track.
     *
     * @param userTrack User / track to create
     * @return User / track ID
     */
    public String create(UserTrack userTrack) {
        // Init user / track data
        userTrack.setId(UUID.randomUUID().toString());
        userTrack.setCreateDate(new Date());
        userTrack.setPlayCount(0);

        // Create user / track
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                " T_USER_TRACK(UST_ID_C, UST_IDUSER_C, UST_IDTRACK_C, UST_CREATEDATE_D)" +
                " values(:id, :userId, :trackId, :createDate)")
                .bind("id", userTrack.getId())
                .bind("userId", userTrack.getUserId())
                .bind("trackId", userTrack.getTrackId())
                .bind("createDate", userTrack.getCreateDate())
                .execute();

        return userTrack.getId();
    }

    /**
     * Gets an active user / track.
     *
     * @param userId User ID
     * @param trackId Track ID
     * @return User / track
     */
    public UserTrack getActiveUserTrack(String userId, String trackId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select " + new UserTrackMapper().getJoinedColumns("ut") +
                "  from T_USER_TRACK ut" +
                "  where ut.UST_DELETEDATE_D is null and ut.UST_IDUSER_C = :userId and ut.UST_IDTRACK_C = :trackId ")
                .bind("userId", userId)
                .bind("trackId", trackId)
                .bind("deleteDate", new Date())
                .mapTo(UserTrack.class)
                .first();
    }

    /**
     * Get a user track or create it if necessary.
     *
     * @param userId User ID
     * @param trackId Track ID
     * @return User / track
     */
    private UserTrack getOrCreateUserTrack(String userId, String trackId) {
        UserTrack userTrack = getActiveUserTrack(userId, trackId);
        if (userTrack == null) {
            userTrack = new UserTrack();
            userTrack.setUserId(userId);
            userTrack.setTrackId(trackId);
            create(userTrack);
        }
        return userTrack;
    }

    /**
     * Updates a userTrack.
     *
     * @param userTrack UserTrack to update
     * @return Updated userTrack
     */
    public UserTrack update(UserTrack userTrack) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_USER_TRACK set " +
                " UST_PLAYCOUNT_N = :playCount," +
                " UST_LIKE_B = :like " +
                " where UST_ID_C = :id and UST_DELETEDATE_D is null")
                .bind("id", userTrack.getId())
                .bind("playCount", userTrack.getPlayCount())
                .bind("like", userTrack.isLike())
                .execute();

        return userTrack;
    }

    /**
     * Increments the number of times the track was played.
     *
     * @param userId User ID
     * @param trackId Track ID
     */
    public void incrementPlayCount(String userId, String trackId) {
        UserTrack userTrack = getOrCreateUserTrack(userId, trackId);
        
        userTrack.setPlayCount(userTrack.getPlayCount() + 1);
        update(userTrack);
    }

    /**
     * Like the track.
     *
     * @param userId User ID
     * @param trackId Track ID
     */
    public void like(String userId, String trackId) {
        UserTrack userTrack = getOrCreateUserTrack(userId, trackId);
        userTrack.setLike(true);
        update(userTrack);
    }

    /**
     * Unlike the track.
     *
     * @param userId User ID
     * @param trackId Track ID
     */
    public void unlike(String userId, String trackId) {
        UserTrack userTrack = getOrCreateUserTrack(userId, trackId);
        userTrack.setLike(false);
        update(userTrack);
    }

    /**
     * Unlike all tracks for a use.
     *
     * @param userId User ID
     */
    public void unlikeAll(String userId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update T_USER_TRACK set " +
                " UST_LIKE_B = :like " +
                " where UST_IDUSER_C = :userId and UST_DELETEDATE_D is null")
                .bind("userId", userId)
                .bind("like", false)
                .execute();
    }
}
