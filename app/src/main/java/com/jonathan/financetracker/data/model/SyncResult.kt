package com.jonathan.financetracker.data.model

/**
 * Aggregate outcome of a [PlaidRepository.syncTransactions] call.
 *
 * [added] is the total transactions written across all healthy items.
 * [failed] is the count of items that errored during sync — each of
 * those items will have its [LinkStatus] updated server-side so the
 * UI can surface a Reconnect affordance for them.
 */
data class SyncResult(
    val added: Int,
    val failed: Int
)
