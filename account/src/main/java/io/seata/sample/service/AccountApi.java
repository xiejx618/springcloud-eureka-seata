package io.seata.sample.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * @author jimin.jm@alibaba-inc.com
 * @date 2019/06/14
 */
@LocalTCC
public interface AccountApi {

    @TwoPhaseBusinessAction(name = "accountService", commitMethod = "commit", rollbackMethod = "rollback")
    void reduce(BusinessActionContext actionContext, String userId, int money);

    boolean commit(BusinessActionContext actionContext);

    boolean rollback(BusinessActionContext actionContext);
}
