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
        return handle.createQuery("select AUT_ID_C, AUT_IDUSER_C, AUT_LONGLASTED_B, AUT_CREATEDATE_D, AUT_LASTCONNECTIONDATE_D" +
                "  from T_AUTHENTICATION_TOKEN" +
                "  where AUT_ID_C = :id")
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
                "  T_AUTHENTICATION_TOKEN(AUT_ID_C, AUT_IDUSER_C, AUT_LONGLASTED_B, AUT_CREATEDATE_D)" +
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
     * @throws Exception
     */
    public void delete(String authenticationTokenId) throws Exception {
        final Handle handle = ThreadLocalContext.get().getHandle();
        AuthenticationToken authenticationToken = get(authenticationTokenId);
        if (authenticationToken != null) {
            handle.createStatement("delete from " +
                    "  T_AUTHENTICATION_TOKEN" +
                    "  where AUT_ID_C = :id")
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
        handle.createStatement("delete from T_AUTHENTICATION_TOKEN AS ato " +
                "  where ato.AUT_IDUSER_C = :userId and ato.AUT_LONGLASTED_B = :longLasted" +
                "  and ato.AUT_LASTCONNECTIONDATE_D < :minDate")
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
        handle.createStatement("update T_AUTHENTICATION_TOKEN ato " +
                "  set ato.AUT_LASTCONNECTIONDATE_D = :currentDate" +
                "  where ato.AUT_ID_C = :id ")
                .bind("currentDate", new Timestamp(new Date().getTime()))
                .bind("id", id)
                .execute();
    }
}
