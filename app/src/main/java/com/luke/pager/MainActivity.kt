package com.luke.pager

import android.database.Cursor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luke.pager.ui.BookViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PagerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagerApp(viewModel: BookViewModel = viewModel()) {
    val books = remember { mutableStateListOf<Pair<Int, String>>() }

    // Fetch books from database
    LaunchedEffect(Unit) {
        val cursor: Cursor = viewModel.getBooks()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            books.add(id to title)
        }
        cursor.close()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pager") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            itemsIndexed(books) { _, book ->
                Text(text = book.second, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
