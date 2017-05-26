package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.constant.ConfigType;
import com.sismics.music.core.model.dbi.Config;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Config result set mapper.
 *
 * @author jtremeaux
 */
public class ConfigMapper implements ResultSetMapper<Config> {
    @Override
    public Config map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Config(
                ConfigType.valueOf(r.getString("id")),
                r.getString("value"));
    }
}
