package com.jonathan.financetracker.data.repository

import com.jonathan.financetracker.data.datasource.PlaidCategoryMappingDataSource
import com.jonathan.financetracker.data.model.PlaidCategoryMapping
import javax.inject.Inject

class PlaidCategoryMappingRepository @Inject constructor(
    private val dataSource: PlaidCategoryMappingDataSource
) {
    suspend fun getMapping(ownerId: String): PlaidCategoryMapping? {
        return dataSource.getMapping(ownerId)
    }

    suspend fun saveMapping(mapping: PlaidCategoryMapping) {
        dataSource.saveMapping(mapping)
    }
}
