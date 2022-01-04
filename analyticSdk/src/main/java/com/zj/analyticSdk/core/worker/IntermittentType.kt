package com.zj.analyticSdk.core.worker
    /**
     * @property REPLACE the new object will replace the old properties always
     * @property STAY_IF_NULL ignore changes if the new property is null.
     * @property REMOVE_IF_NULL remove the old property if the new is null.
     * */
    enum class IntermittentType { REPLACE, STAY_IF_NULL, REMOVE_IF_NULL }