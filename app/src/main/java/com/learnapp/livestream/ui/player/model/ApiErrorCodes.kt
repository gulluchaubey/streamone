package com.learnapp.livestream.ui.player.model

import live.hms.video.error.ErrorCodes

enum class ApiErrorCodes(val code: Int, val reason: String, val description: String) {
    TOKEN_ERROR(401, "Invalid Token", "Something Went Wrong"),
    ROOM_DISABLED(400, "Room Disabled", "Workshop room is disabled"),
    NO_INTERNET_CONNECTIVITY(0, "No Internet Connection", "No Internet Connection"),
    ENDPOINT_UNREACHABLE(
        ErrorCodes.InitAPIErrors.cEndpointUnreachable,
        "Endpoint is not reachable",
        "Something Went Wrong"
    )
}
