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
public class StorageService implements StorageApi {

    private static Map<String,Map<String,Object>> data = new ConcurrentHashMap<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public void deduct(BusinessActionContext actionContext, String commodityCode, int count) {
        jdbcTemplate.update("update storage_tbl set count = count - ? where commodity_code = ?",
                new Object[]{count, commodityCode});

        //提交事务成功
        final Map<String, Object> actionContextMap = new HashMap<>();
        actionContextMap.put("commodityCode", commodityCode);
        actionContextMap.put("count", count);
        data.put(actionContext.getXid(), actionContextMap);

    }

    @Override
    public boolean commit(BusinessActionContext actionContext) {
        final String xid = actionContext.getXid();
        System.out.println("TccActionOne commit, xid:" + xid);
        data.remove(xid);
        return true;
    }

    @Override
    public boolean rollback(BusinessActionContext actionContext) {
        final String xid = actionContext.getXid();
        System.out.println("TccActionOne commit, xid:" + xid);

        final Map<String, Object> actionContextMap = data.get(xid);
        final Object commodityCode = actionContextMap.get("commodityCode");
        final Object count = actionContextMap.get("count");
        //如果事务已提交,就回滚
        if (commodityCode != null && count != null) {
            jdbcTemplate.update("update storage_tbl set count = count + ? where commodity_code = ?", new Object[]{count, commodityCode});
        }
        data.remove(xid);
        return true;
    }
}
