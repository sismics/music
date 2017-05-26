package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.Directory;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Directory result set mapper.
 *
 * @author jtremeaux
 */
public class DirectoryMapper implements ResultSetMapper<Directory> {
    @Override
    public Directory map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Directory(
                r.getString("id"),
                r.getString("location"),
                r.getTimestamp("disabledate"),
                r.getTimestamp("createdate"),
                r.getTimestamp("deletedate"));
    }
}
