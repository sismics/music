package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.RoleBaseFunction;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Album result set mapper.
 *
 * @author jtremeaux
 */
public class RoleBaseFunctionMapper implements ResultSetMapper<RoleBaseFunction> {
    @Override
    public RoleBaseFunction map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new RoleBaseFunction(
                r.getString("RBF_ID_C"),
                r.getString("RBF_IDROLE_C"),
                r.getString("RBF_IDBASEFUNCTION_C"),
                r.getDate("RBF_CREATEDATE_D"),
                r.getDate("RBF_DELETEDATE_D"));
    }
}
