package domain.model

import org.gutierrethiago.raxafootballkotlinmulti.database.PlayerSoccer

data class PlayerSoccerSelected(
    val player: PlayerSoccer,
    var selected: Boolean = false
)
