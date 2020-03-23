package io.seata.sample.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.sample.feign.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jimin.jm@alibaba-inc.com
 * @date 2019/06/14
 */
@Service
public class OrderService implements OrderApi {
    private static Map<String, Map<String,Object>> data = new ConcurrentHashMap<>();
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AtomicInteger atomicInteger = new AtomicInteger();

    @Transactional
    @Override
    public void create(BusinessActionContext actionContext,
                       String userId, String commodityCode, Integer count) {
        //假设不冲突
        int id = atomicInteger.getAndIncrement();

        int orderMoney = count * 100;
        jdbcTemplate.update("insert order_tbl(id,user_id,commodity_code,count,money) values(?,?,?,?,?)",
                new Object[]{id, userId, commodityCode, count, orderMoney});
        userFeignClient.reduce(userId, orderMoney);

        //代表事务提交成功
        //提交事务成功
        final Map<String, Object> actionContextMap = new HashMap<>();
        actionContext.getActionContext().put("id", id);
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
        final Object id = actionContextMap.get("id");
        //如果事务已提交,就回滚
        if (id != null) {
            jdbcTemplate.update("delete order_tbl where id= ?",id);
        }
        data.remove(xid);
        return true;
    }
}
