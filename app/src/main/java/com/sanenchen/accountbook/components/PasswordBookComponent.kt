package com.sanenchen.accountbook.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanenchen.accountbook.databases.AppDatabase
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.Icon
import kiwi.orbit.compose.ui.controls.IconButton
import kiwi.orbit.compose.ui.controls.Text

@Composable
fun PasswordBookComponent(options: Int) {
    val queryResult = if(options == -1) AppDatabase.database.PasswordDataDao().queryAll().collectAsState(listOf()).value
            else AppDatabase.database.PasswordDataDao().queryWithGroup(options).collectAsState(listOf()).value

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = queryResult) {
            PasswordItem(title = it.title, user = it.user, password = it.password)
        }
    }
}

@Composable
fun PasswordItem(title: String, user: String, password: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        var isShowingUser by remember { mutableStateOf(true) }
        Box(
            Modifier
                .weight(1f)
                .clickable { isShowingUser = !isShowingUser }) {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp)) {
                Text(title, fontSize = 20.sp)
                Spacer(modifier = Modifier.padding(top = 4.dp))
                AnimatedVisibility(visible = isShowingUser) {
                    Text(user, color = OrbitTheme.colors.content.minor) // 账号
                }
                AnimatedVisibility(visible = !isShowingUser) {
                    Text(password, color = OrbitTheme.colors.content.minor) // 密码
                }
            }
        }
        IconButton(onClick = { /*TODO*/ }, modifier = Modifier
            .padding(end = 16.dp)
            .size(48.dp)) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = null, tint = OrbitTheme.colors.primary.normal, modifier = Modifier.size(24.dp))
        }
    }
}