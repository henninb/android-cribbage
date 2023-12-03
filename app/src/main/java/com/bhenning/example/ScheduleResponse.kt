package com.bhenning.example

data class ScheduleResponse(
    val MatchNumber: Int,
    val RoundNumber: Int,
    val DateUtc: String,
    val Location: String,
    val HomeTeam: String,
    val AwayTeam: String,
    val Group: String?, // Use String? if it can be null in the JSON
    val HomeTeamScore: Int?, // Use Int? if it can be null in the JSON
    val AwayTeamScore: Int? // Use Int? if it can be null in the JSON
)