package com.learnapp.livestream.data.models

enum class ApiResponseError(val errorName: String) {
    SOMETHING_WENT_WRONG("Something went wrong"),
    CHECK_INTERNET_CONNECTION("Check Internet Connection"),
    INVALID_TOKEN("Invalid Token"),
    ACCOUNT_NOT_VERIFIED("Account not Verified"),
    PAYMENT_FAILED_CANCELLED("Payment failed / cancelled"),
    NO_OFFLINE_COURSES("No Offline Courses"),
    INTERNET_CONNECTION_FAILED("Internet Connection Failed")
}
