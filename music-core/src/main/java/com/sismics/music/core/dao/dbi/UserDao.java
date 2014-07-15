package com.sismics.music.core.dao.dbi;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;

import com.google.common.base.Joiner;
import com.sismics.music.core.dao.dbi.criteria.UserCriteria;
import com.sismics.music.core.dao.dbi.dto.UserDto;
import com.sismics.music.core.dao.dbi.mapper.UserMapper;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.util.dbi.ColumnIndexMapper;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.music.core.util.dbi.QueryUtil;
import com.sismics.music.core.util.dbi.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

/**
 * User DAO.
 * 
 * @author jtremeaux
 */
public class UserDao {
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
                "  from T_USER u" +
                "  where u.USE_USERNAME_C = :username and u.USE_DELETEDATE_D is null")
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
     * @throws Exception
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
                " T_USER(USE_ID_C, USE_IDLOCALE_C, USE_IDROLE_C, USE_USERNAME_C, USE_PASSWORD_C, USE_EMAIL_C, USE_FIRSTCONNECTION_B, USE_CREATEDATE_D)" +
                " values(:id, :localeId, :roleId, :username, :password, :email, :firstConnection, :createDate)")
                .bind("id", user.getId())
                .bind("localeId", user.getLocaleId())
                .bind("roleId", user.getRoleId())
                .bind("username", user.getUsername())
                .bind("password", user.getPassword())
                .bind("email", user.getEmail())
                .bind("firstConnection", user.isFirstConnection())
                .bind("createDate", user.getCreateDate())
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
        handle.createStatement("update T_USER u set " +
                " u.USE_IDLOCALE_C = :localeId," +
                " u.USE_EMAIL_C = :email, " +
                " u.USE_FIRSTCONNECTION_B = :firstConnection " +
                " where u.USE_ID_C = :id and u.USE_DELETEDATE_D is null")
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
        handle.createStatement("update T_USER u set " +
                " u.USE_PASSWORD_C = :password " +
                " where u.USE_ID_C = :id and u.USE_DELETEDATE_D is null")
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
        handle.createStatement("update T_USER u set " +
                " u.USE_LASTFMSESSIONTOKEN_C = :lastFmSessionToken " +
                " where u.USE_ID_C = :id and u.USE_DELETEDATE_D is null")
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
                "  from T_USER u" +
                "  where u.USE_ID_C = :id and u.USE_DELETEDATE_D is null")
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
                "  from T_USER u" +
                "  where u.USE_USERNAME_C = :username and u.USE_DELETEDATE_D is null")
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
                "  from T_USER u" +
                "  where u.USE_PASSWORDRESETKEY_C = :passwordResetKey and u.USE_DELETEDATE_D is null")
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
        handle.createStatement("update T_USER u" +
                "  set u.USE_DELETEDATE_D = :deleteDate" +
                "  where u.USE_USERNAME_C = :username and u.USE_DELETEDATE_D is null")
                .bind("username", username)
                .bind("deleteDate", new Date())
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

    /**
     * Searches users by criteria.
     *
     * @param paginatedList Paginated list (populated by side effects)
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     */
    public void findByCriteria(PaginatedList<UserDto> paginatedList, UserCriteria criteria, SortCriteria sortCriteria) {
        QueryParam queryParam = getQueryParam(criteria);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria, true);
        List<UserDto> userDtoList = assembleResultList(l);
        paginatedList.setResultList(userDtoList);
    }

    /**
     * Searches users by criteria.
     *
     * @param criteria Search criteria
     * @return List of users
     */
    public List<UserDto> findByCriteria(UserCriteria criteria) {
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
    private QueryParam getQueryParam(UserCriteria criteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        StringBuilder sb = new StringBuilder("select u.USE_ID_C as c0, u.USE_USERNAME_C as c1, u.USE_EMAIL_C as c2, u.USE_CREATEDATE_D as c3, u.USE_IDLOCALE_C as c4");
        sb.append(" from T_USER u ");

        // Add search criterias
        List<String> criteriaList = new ArrayList<String>();
        if (criteria.isLastFmSessionTokenNotNull()) {
            criteriaList.add("USE_LASTFMSESSIONTOKEN_C is not null");
        }
        criteriaList.add("u.USE_DELETEDATE_D is null");

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        return queryParam;
    }

    /**
     * Assemble the query results.
     *
     * @param l Query results as a table
     * @return Query results as a list of domain objects
     */
    private List<UserDto> assembleResultList(List<Object[]> l) {
        List<UserDto> userDtoList = new ArrayList<UserDto>();
        for (Object[] o : l) {
            int i = 0;
            UserDto userDto = new UserDto();
            userDto.setId((String) o[i++]);
            userDto.setUsername((String) o[i++]);
            userDto.setEmail((String) o[i++]);
            userDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            userDto.setLocaleId((String) o[i++]);
            userDtoList.add(userDto);
        }
        return userDtoList;
    }
}
