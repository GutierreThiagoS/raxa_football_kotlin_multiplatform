package framework.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterComponent(onFilterSelected: (String) -> Unit) {
    val categories = listOf("Todos", "Eletrônicos", "Livros", "Vestuário")
    val selectedCategory = remember { mutableStateOf("Todos") }

    Column(modifier = Modifier.padding(16.dp)) {
        categories.forEach { category ->
            Button(
                onClick = {
                    selectedCategory.value = category
                    onFilterSelected(category)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = category)
            }
        }
    }
}

@Composable
fun FilterSpinner() {
    val categories = listOf("Todos", "Eletrônicos", "Livros", "Vestuário")
    val expanded = remember { mutableStateOf(false) }
    val selectedCategory = remember { mutableStateOf("Todos") }

    Column(modifier = Modifier.padding(16.dp)) {
        ButtonOutline(onClick = { expanded.value = true }) {
            Icon(Icons.Default.ArrowDropDown, null)
            Text(text = selectedCategory.value)
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(onClick = {
                    selectedCategory.value = category
                    expanded.value = false
                }) {
                    Text(text = category)
                }
            }
        }
    }
}
