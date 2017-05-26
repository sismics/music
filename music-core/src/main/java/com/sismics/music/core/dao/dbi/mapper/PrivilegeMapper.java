package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Privilege;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Basefunction result set mapper.
 *
 * @author jtremeaux
 */
public class PrivilegeMapper implements ResultSetMapper<Privilege> {
    @Override
    public Privilege map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Privilege(r.getString("id"));
    }
}
