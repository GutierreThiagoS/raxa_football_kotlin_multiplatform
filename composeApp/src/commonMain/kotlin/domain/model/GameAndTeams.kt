package domain.model

import org.gutierrethiago.raxafootballkotlinmulti.database.Game

data class GameAndTeams(
    val game: Game,
    val teamAndPlayers1: TeamAndPlayer,
    val teamAndPlayers2: TeamAndPlayer,
)
