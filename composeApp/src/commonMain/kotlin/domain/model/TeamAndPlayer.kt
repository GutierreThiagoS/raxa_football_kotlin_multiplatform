package domain.model

import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerInTeam
import org.gutierrethiago.raxafootballkotlinmulti.database.Team

data class TeamAndPlayer(
    val team: Team,
    val players: List<PlayerInTeam>
)
