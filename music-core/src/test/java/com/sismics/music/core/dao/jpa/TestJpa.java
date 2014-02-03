package com.sismics.music.core.dao.jpa;

import com.sismics.music.BaseTransactionalTest;
import com.sismics.music.core.model.jpa.User;
import com.sismics.music.core.util.TransactionUtil;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Tests the persistance layer.
 * 
 * @author jtremeaux
 */
public class TestJpa extends BaseTransactionalTest {
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
