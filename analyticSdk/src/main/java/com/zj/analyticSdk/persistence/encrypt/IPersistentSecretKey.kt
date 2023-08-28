package com.zj.analyticSdk.persistence.encrypt

interface IPersistentSecretKey {
    /**
     * Store the public key
     */
    fun saveSecretKey(secreteKey: SecreteKey?)

    /**
     * Get public key
     */
    fun loadSecretKey(): SecreteKey?
}