package com.sismics.music;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class TestJdbi {
    @Test
    public void jdbiTest() throws Exception {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        DBI dbi = new DBI(cpds);
        Handle h = dbi.open();

        h.execute("create table something (id int primary key, name varchar(100))");
        h.execute("insert into something (id, name) values (?, ?)", 1, "Brian");

        h.close();
    }
}
