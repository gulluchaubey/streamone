package com.learnapp.livestream.ui.player.model

import com.learnapp.livestream.R

enum class NetworkQualityCode(val code: Int, val message: String, val resourceId: Int) {
    TEST_TIMEOUT(-1, "Test timeout", R.drawable.ic_network_status_0),
    NETWORK_FAILURE(0, "Very bad network or network check failure", R.drawable.ic_network_status_0),
    POOR_NETWORK(1, "Poor network", R.drawable.ic_network_status_1),
    BAD_NETWORK(2, "Bad network", R.drawable.ic_network_status_2),
    AVERAGE(3, "Average", R.drawable.ic_network_status_3),
    GOOD(4, "Good", R.drawable.ic_network_status_4),
    BEST(5, "Best", R.drawable.ic_network_status_4)
}
