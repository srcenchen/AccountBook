package com.sanenchen.accountbook.components

import android.app.Activity
import android.content.Intent
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
import com.sanenchen.accountbook.activities.PasswordDetailActivity
import com.sanenchen.accountbook.databases.AppDatabase
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.BadgeSuccessSubtle
import kiwi.orbit.compose.ui.controls.Icon
import kiwi.orbit.compose.ui.controls.IconButton
import kiwi.orbit.compose.ui.controls.Text

/**
 * 密码展示组件
 * @author sanenchen
 */
@Composable
fun PasswordBookComponent(options: Int, activity: Activity) {
    val queryResult = if (options == -1) AppDatabase.database.PasswordDataDao().queryAll().collectAsState(listOf()).value
    else AppDatabase.database.PasswordDataDao().queryWithGroup(options).collectAsState(listOf()).value

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = queryResult) {
            val groupName = if (it.groupID != -1) AppDatabase.database.PasswordGroupDao().queryWithID(it.groupID)[0].groupName else "无分组"
            PasswordItem(title = it.title, user = it.user, password = it.password, id = it.id, activity = activity, groupName = groupName)
        }
    }
}

/**
 * 密码分项
 */
@Composable
fun PasswordItem(title: String, user: String, password: String, groupName: String,id: Int, activity: Activity) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        var isShowingUser by remember { mutableStateOf(true) }
        Box(
                Modifier
                        .weight(1f)
                        .clickable { isShowingUser = !isShowingUser }
                        .padding(end = 16.dp)) {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgeSuccessSubtle(Modifier.padding(end = 8.dp)) {
                        Text(groupName)
                    }
                    Text(title, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.padding(top = 4.dp))
                AnimatedVisibility(visible = isShowingUser) {
                    Text(user, color = OrbitTheme.colors.content.minor) // 账号
                }
                AnimatedVisibility(visible = !isShowingUser) {
                    Text(password, color = OrbitTheme.colors.content.minor) // 密码
                }
            }
        }
        IconButton( // 密码详情
                onClick = {
                    val intent = Intent(activity, PasswordDetailActivity::class.java)
                    intent.putExtra("dataID", id)
                    activity.startActivity(intent)
                }, modifier = Modifier
                .size(48.dp)
                .padding(end = 16.dp)
        ) {
            Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = OrbitTheme.colors.primary.normal,
                    modifier = Modifier.size(24.dp)
            )
        }
    }
}