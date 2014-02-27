package com.sismics.music.core.dao.dbi;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.music.BaseTransactionalTest;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.util.TransactionUtil;

/**
 * Tests the persistance layer.
 * 
 * @author jtremeaux
 */
public class TestDbi extends BaseTransactionalTest {
    @Test
    public void testJpa() throws Exception {
        // Create a user
        UserDao userDao = new UserDao();
        User user = new User();
        user.setUsername("username");
        user.setEmail("toto@music.com");
        user.setLocaleId("fr_FR");
        user.setRoleId("user");
        String id = userDao.create(user);
        
        TransactionUtil.commit();

        // Search a user by his ID
        user = userDao.getActiveById(id);
        Assert.assertNotNull(user);
        Assert.assertEquals("toto@music.com", user.getEmail());
    }
}
