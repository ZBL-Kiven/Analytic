package com.zj.analyticSdk.persistence.encrypt

data class SecreteKey(
    /**
     * Public key
     */
    var key: String?,
    /**
     * Public key secret key version
     */
    var version: Int) {

    override fun toString(): String {
        return "{ key=\"$key\", \"version\"=$version}"
    }
}