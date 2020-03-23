package io.seata.sample.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
@LocalTCC
public interface StorageApi {

    @TwoPhaseBusinessAction(name = "storageService", commitMethod = "commit", rollbackMethod = "rollback")
    void deduct(BusinessActionContext actionContext, String commodityCode, int count);

    boolean commit(BusinessActionContext actionContext);

    boolean rollback(BusinessActionContext actionContext);
}
