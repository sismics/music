package com.sismics.music.core.dao.jpa;

import com.google.common.collect.Sets;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Role base functions DAO.
 * 
 * @author jtremeaux
 */
public class RoleBaseFunctionDao {
    /**
     * Find the set of base functions of a role.
     * 
     * @param roleId Role ID
     * @return Set of base functions
     */
    public Set<String> findByRoleId(String roleId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        List<Map<String, Object>> resultList = handle.createQuery("select rbf.RBF_IDBASEFUNCTION_C " +
                "  from T_ROLE_BASE_FUNCTION rbf, T_ROLE r" +
                "  where rbf.RBF_IDROLE_C = :roleId and rbf.RBF_DELETEDATE_D is null" +
                "  and r.ROL_ID_C = rbf.RBF_IDROLE_C and r.ROL_DELETEDATE_D is null")
                .bind("roleId", roleId)
                .list();
        Set<String> roleSet = Sets.newHashSet();
        for (Map<String, Object> role : resultList) {
            roleSet.add((String) role.get("RBF_IDBASEFUNCTION_C"));
        }
        return roleSet;
    }
}
