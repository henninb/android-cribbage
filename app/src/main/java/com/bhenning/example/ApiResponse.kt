package com.bhenning.example


//data class ApiResponse(
//    val matchNumber: Int,
//    val location: String,
//    val homeTeam: String
//
//
//    //[{"MatchNumber":14,"RoundNumber":1,"DateUtc":"2023-10-13 00:00:00Z","Location":"Xcel Energy Center","HomeTeam":"Minnesota Wild","AwayTeam":"Florida Panthers","Group":null,"HomeTeamScore":null,"AwayTeamScore":null},
//)


data class ApiResponse(
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