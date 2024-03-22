package framework.presentation.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun SearchComponent(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { newValue -> searchQuery.value = newValue },
            label = { Text("Pesquisar") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                IconButton(
                    onClick = {
                        searchQuery.value = ""
                        onSearch(searchQuery.value)
                        keyboardController?.hide()
                    }
                ) {
                    Icon(Icons.Default.Clear, null)
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(searchQuery.value)
                    keyboardController?.hide()
                }
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )
    }
}