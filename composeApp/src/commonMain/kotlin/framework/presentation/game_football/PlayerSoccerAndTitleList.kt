package framework.presentation.game_football

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PlayerSoccerAndTitleList(list: List<String>) {
    Column {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(end = 10.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0x12000000))
        ) {
            Text(
                "Time 1",
                modifier = Modifier.fillMaxWidth()
                    .padding(10.dp),
                fontWeight = FontWeight.Bold
            )
        }
        LazyColumn {
            itemsIndexed(list) { i, item ->
                Text(
                    item,
                    modifier = Modifier.fillMaxWidth()
                        .padding(10.dp)
                )
                if (i < list.lastIndex) {
                    Divider(color = Color(0x55000000), thickness = 2.dp)
                }
            }
        }
    }


}