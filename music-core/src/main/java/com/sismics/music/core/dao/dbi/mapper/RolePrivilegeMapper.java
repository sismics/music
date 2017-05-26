package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.RolePrivilege;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class RolePrivilegeMapper implements ResultSetMapper<RolePrivilege> {
    @Override
    public RolePrivilege map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new RolePrivilege(
                r.getString("id"),
                r.getString("role_id"),
                r.getString("privilege_id"),
                r.getTimestamp("createdate"),
                r.getTimestamp("deletedate"));
    }
}
