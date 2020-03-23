package io.seata.sample.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jimin.jm@alibaba-inc.com
 * @date 2019/06/14
 */
@Service
public class AccountService implements AccountApi {
    private static Map<String,Map<String,Object>> data = new ConcurrentHashMap<>();
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public void reduce(BusinessActionContext actionContext, String userId, int money) {
        jdbcTemplate.update("update account_tbl set money = money - ? where user_id = ?", new Object[]{money, userId});


        //提交事务成功
        final Map<String, Object> actionContextMap = new HashMap<>();
        actionContextMap.put("userId", userId);
        actionContextMap.put("money", money);
        data.put(actionContext.getXid(), actionContextMap);


    }

    public boolean commit(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        System.out.println("TccActionOne commit, xid:" + xid);

        data.remove(xid);
        return true;
    }

    public boolean rollback(BusinessActionContext actionContext) {
        String xid = actionContext.getXid();
        System.out.println("TccActionOne rollback, xid:" + xid);

        final Map<String, Object> actionContextMap = data.get(xid);
        final Object userId = actionContextMap.get("userId");
        final Object money = actionContextMap.get("money");
        //如果事务已提交,就回滚
        if (userId != null && money != null) {
            jdbcTemplate.update("update account_tbl set money = money + ? where user_id = ?", new Object[]{money, userId});
        }
        data.remove(xid);
        return true;
    }
}
