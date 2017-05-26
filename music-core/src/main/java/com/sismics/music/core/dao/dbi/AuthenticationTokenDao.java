package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.model.dbi.AuthenticationToken;
import com.sismics.util.context.ThreadLocalContext;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.Handle;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * Authentication token DAO.
 * 
 * @author jtremeaux
 */
public class AuthenticationTokenDao {
    /**
     * Gets an authentication token.
     * 
     * @param id Authentication token ID
     * @return Authentication token
     */
    public AuthenticationToken get(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        return handle.createQuery("select id, user_id, longlasted, createdate, lastconnectiondate" +
                "  from t_authentication_token" +
                "  where id = :id")
                .bind("id", id)
                .mapTo(AuthenticationToken.class)
                .first();
    }

    /**
     * Creates a new authentication token.
     * 
     * @param authenticationToken Authentication token
     * @return Authentication token ID
     */
    public String create(AuthenticationToken authenticationToken) {
        authenticationToken.setId(UUID.randomUUID().toString());
        authenticationToken.setCreateDate(new Date());

        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                "  t_authentication_token(id, user_id, longlasted, createdate)" +
                "  values(:id, :userId, :longLasted, :createDate)")
                .bind("id", authenticationToken.getId())
                .bind("userId", authenticationToken.getUserId())
                .bind("longLasted", authenticationToken.isLongLasted())
                .bind("createDate", new Timestamp(authenticationToken.getCreateDate().getTime()))
                .execute();
            
        return authenticationToken.getId();
    }

    /**
     * Deletes the authentication token.
     * 
     * @param authenticationTokenId Authentication token ID
     */
    public void delete(String authenticationTokenId) throws Exception {
        final Handle handle = ThreadLocalContext.get().getHandle();
        AuthenticationToken authenticationToken = get(authenticationTokenId);
        if (authenticationToken != null) {
            handle.createStatement("delete from " +
                    "  t_authentication_token" +
                    "  where id = :id")
                    .bind("id", authenticationToken.getId())
                    .execute();
        } else {
            throw new Exception("Token not found: " + authenticationTokenId);
        }
    }

    /**
     * Deletes old short lived tokens.
     *
     * @param userId User ID
     */
    public void deleteOldSessionToken(String userId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("delete from t_authentication_token as ato " +
                "  where ato.user_id = :userId and ato.longlasted = :longLasted" +
                "  and ato.lastconnectiondate < :minDate")
                .bind("userId", userId)
                .bind("longLasted", false)
                .bind("minDate", new Timestamp(DateTime.now().minusDays(1).getMillis()))
                .execute();
    }

    /**
     * Deletes old short lived tokens.
     *
     * @param id Token id
     */
    public void updateLastConnectionDate(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_authentication_token ato " +
                "  set ato.lastconnectiondate = :currentDate" +
                "  where ato.id = :id ")
                .bind("currentDate", new Timestamp(new Date().getTime()))
                .bind("id", id)
                .execute();
    }
}
