package com.example.baraclan.mentalchallengemath_namepending.models

data class UserStatistics(
    val username: String,
    var singleplayerWins: Int = 0,
    var singleplayerLosses: Int = 0,
    var singleplayerTies: Int = 0,
    var localMultiplayerWinsRed: Int = 0,
    var localMultiplayerLossesRed: Int = 0,
    var localMultiplayerTiesRed: Int = 0,
    var localMultiplayerWinsBlue: Int = 0,
    var localMultiplayerLossesBlue: Int = 0,
    var localMultiplayerTiesBlue: Int = 0,
    var onlineMultiplayerWins: Int = 0,
    var onlineMultiplayerLosses: Int = 0,
    var onlineMultiplayerTies: Int = 0
) {
    fun getTotalSingleplayerGames(): Int = singleplayerWins + singleplayerLosses + singleplayerTies
    fun getTotalLocalMultiplayerGamesRed(): Int = localMultiplayerWinsRed + localMultiplayerLossesRed + localMultiplayerTiesRed
    fun getTotalLocalMultiplayerGamesBlue(): Int = localMultiplayerWinsBlue + localMultiplayerLossesBlue + localMultiplayerTiesBlue
    fun getTotalOnlineMultiplayerGames(): Int = onlineMultiplayerWins + onlineMultiplayerLosses + onlineMultiplayerTies
}

// Singleton to store current user statistics (in a real app, this would be persisted)
object UserStatsManager {
    private var currentStats: UserStatistics? = null
    
    fun initializeStats(username: String) {
        currentStats = UserStatistics(username = username)
    }
    
    fun getStats(): UserStatistics? = currentStats
    
    fun updateStats(update: (UserStatistics) -> Unit) {
        currentStats?.let { update(it) }
    }
}
