package com.sismics.util.dbi;

import org.skife.jdbi.v2.util.TypedMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Map to an Object.
 *
 * @author jtremeaux
 */
public class ObjectMapper extends TypedMapper<Object> {
    /**
     * An instance which extracts value from the first field
     */
    public static final ObjectMapper FIRST = new ObjectMapper(1);

    /**
     * Create a new instance which extracts the value positionally
     * in the result set
     *
     * @param index 1 based column index into the result set
     */
    public ObjectMapper(int index)
    {
        super(index);
    }

    /**
     * Create a new instance which extracts the value from the first column
     */
    public ObjectMapper()
    {
        super(1);
    }

    /**
     * Create a new instance which extracts the value by name or alias from the result set
     *
     * @param name The name or alias for the field
     */
    public ObjectMapper(String name)
    {
        super(name);
    }

    @Override
    protected Object extractByName(ResultSet r, String name) throws SQLException
    {
        return r.getObject(name);
    }

    @Override
    protected Object extractByIndex(ResultSet r, int index) throws SQLException
    {
        return r.getObject(index);
    }
}