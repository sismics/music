package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.dao.dbi.criteria.UserCriteria;
import com.sismics.music.core.dao.dbi.dto.UserDto;
import com.sismics.music.core.dao.dbi.mapper.UserDtoMapper;
import com.sismics.music.core.dao.dbi.mapper.UserMapper;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.BaseDao;
import com.sismics.util.dbi.filter.FilterCriteria;
import org.mindrot.jbcrypt.BCrypt;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import java.sql.Timestamp;
import java.util.*;

/**
 * User DAO.
 * 
 * @author jtremeaux
 */
public class UserDao extends BaseDao<UserDto, UserCriteria> {
    @Override
    protected QueryParam getQueryParam(UserCriteria criteria, FilterCriteria filterCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        StringBuilder sb = new StringBuilder("select u.id as c0, u.username as c1, u.email as c2, u.createdate as c3, u.locale_id as c4");
        sb.append("  from t_user u ");

        // Add search criterias
        List<String> criteriaList = new ArrayList<>();
        if (criteria.isLastFmSessionTokenNotNull()) {
            criteriaList.add("lastfmsessiontoken is not null");
        }
        criteriaList.add("u.deletedate is null");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, null, filterCriteria, new UserDtoMapper());
    }

    /**
     * Authenticates an user.
     * 
     * @param username User login
     * @param password User password
     * @return ID of the authenticated user or null
     */
    public String authenticate(String username, String password) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Query<User> q = handle.createQuery("select " + new UserMapper().getJoinedColumns("u") +
                "  from t_user u" +
                "  where u.username = :username and u.deletedate is null")
                .bind("username", username)
                .mapTo(User.class);
        User user = q.first();
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            return null;
        }
        return user.getId();
    }
    
    /**
     * Creates a new user.
     * 
     * @param user User to create
     * @return User ID
     */
    public String create(User user) throws Exception {
        // Init user data
        user.setId(UUID.randomUUID().toString());
        user.setPassword(hashPassword(user.getPassword()));
        user.setCreateDate(new Date());

        // Checks for user unicity
        if (getActiveByUsername(user.getUsername()) != null) {
            throw new Exception("AlreadyExistingUsername");
        }

        // Create user
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("insert into " +
                " t_user(id, locale_id, role_id, username, password, email, firstconnection, createdate)" +
                " values(:id, :localeId, :roleId, :username, :password, :email, :firstConnection, :createDate)")
                .bind("id", user.getId())
                .bind("localeId", user.getLocaleId())
                .bind("roleId", user.getRoleId())
                .bind("username", user.getUsername())
                .bind("password", user.getPassword())
                .bind("email", user.getEmail())
                .bind("firstConnection", user.isFirstConnection())
                .bind("createDate", new Timestamp(user.getCreateDate().getTime()))
                .execute();

        return user.getId();
    }
    
    /**
     * Updates a user.
     * 
     * @param user User to update
     * @return Updated user
     */
    public User update(User user) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_user u set " +
                " u.locale_id = :localeId," +
                " u.email = :email, " +
                " u.firstconnection = :firstConnection " +
                " where u.id = :id and u.deletedate is null")
                .bind("id", user.getId())
                .bind("localeId", user.getLocaleId())
                .bind("email", user.getEmail())
                .bind("firstConnection", user.isFirstConnection())
                .execute();

        return user;
    }
    
    /**
     * Update the user password.
     * 
     * @param user User to update
     * @return Updated user
     */
    public User updatePassword(User user) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_user u set " +
                " u.password = :password " +
                " where u.id = :id and u.deletedate is null")
                .bind("id", user.getId())
                .bind("password", hashPassword(user.getPassword()))
                .execute();

        return user;
    }

    /**
     * Update the user Last.fm session token.
     *
     * @param user User to update
     * @return Updated user
     */
    public User updateLastFmSessionToken(User user) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_user u set " +
                " u.lastfmsessiontoken = :lastFmSessionToken " +
                " where u.id = :id and u.deletedate is null")
                .bind("id", user.getId())
                .bind("lastFmSessionToken", user.getLastFmSessionToken())
                .execute();

        return user;
    }

    /**
     * Gets a user by its ID.
     * 
     * @param id User ID
     * @return User
     */
    public User getActiveById(String id) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Query<User> q = handle.createQuery("select " + new UserMapper().getJoinedColumns("u") +
                "  from t_user u" +
                "  where u.id = :id and u.deletedate is null")
                .bind("id", id)
                .mapTo(User.class);
        return q.first();
    }
    
    /**
     * Gets an active user by its username.
     * 
     * @param username User's username
     * @return User
     */
    public User getActiveByUsername(String username) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Query<User> q = handle.createQuery("select " + new UserMapper().getJoinedColumns("u") +
                "  from t_user u" +
                "  where u.username = :username and u.deletedate is null")
                .bind("username", username)
                .mapTo(User.class);
        return q.first();
    }
    
    /**
     * Gets an active user by its password recovery token.
     * 
     * @param passwordResetKey Password recovery token
     * @return User
     */
    public User getActiveByPasswordResetKey(String passwordResetKey) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        Query<User> q = handle.createQuery("select " + new UserMapper().getJoinedColumns("u") +
                "  from t_user u" +
                "  where u.passwordresetkey = :passwordResetKey and u.deletedate is null")
                .bind("passwordResetKey", passwordResetKey)
                .mapTo(User.class);
        return q.first();
    }
    
    /**
     * Deletes a user.
     * 
     * @param username User's username
     */
    public void delete(String username) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_user u" +
                "  set u.deletedate = :deleteDate" +
                "  where u.username = :username and u.deletedate is null")
                .bind("username", username)
                .bind("deleteDate", new Timestamp(new Date().getTime()))
                .execute();
    }

    /**
     * Hash the user's password.
     * 
     * @param password Clear password
     * @return Hashed password
     */
    protected String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
