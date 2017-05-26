package com.sismics.music.core.dao.dbi;

import com.google.common.collect.Sets;
import com.sismics.util.context.ThreadLocalContext;
import org.skife.jdbi.v2.Handle;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Role privileges DAO.
 * 
 * @author jtremeaux
 */
public class RolePrivilegeDao {
    /**
     * Find the set of privileges of a role.
     * 
     * @param roleId Role ID
     * @return Set of privileges
     */
    public Set<String> findByRoleId(String roleId) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        List<Map<String, Object>> resultList = handle.createQuery("select rpr.privilege_id " +
                "  from t_role_privilege rpr, t_role r" +
                "  where rpr.role_id = :roleId and rpr.deletedate is null" +
                "  and r.id = rpr.role_id and r.deletedate is null")
                .bind("roleId", roleId)
                .list();
        Set<String> roleSet = Sets.newHashSet();
        for (Map<String, Object> role : resultList) {
            roleSet.add((String) role.get("privilege_id"));
        }
        return roleSet;
    }
}
