package com.sanenchen.accountbook.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedButton
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanenchen.accountbook.R
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.*

/**
 * 详细信息
 * @author sanenchen
 */
class PasswordDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountBookTheme {
                Scaffold(topBar = { TopAppBar(title = { Text("详细信息") }, onNavigateUp = { this.finish() }) }) {
                    val dataID = intent.getIntExtra("dataID", 0)
                    Column(Modifier.padding(it)) {
                        MainUI(dataID = dataID)
                    }
                }
            }
        }
    }

    @Composable
    fun MainUI(dataID: Int) {
        val passwordData = AppDatabase.database.PasswordDataDao().queryWithID(id = dataID)[0]
        val title = passwordData.title
        val user = passwordData.user
        val password = passwordData.password
        val remark = passwordData.remark
        val groupName = if (passwordData.groupID != -1) AppDatabase.database.PasswordGroupDao().queryWithID(passwordData.groupID)[0].groupName else "无分组"
        PassIntroduction(dataID, title, user, groupName) // 密码介绍
        Divider() // 长长的分割线
        PassDetail(title, user, password, remark) // 密码详细信息
    }

    /**
     * 密码介绍
     */
    @Composable
    fun PassIntroduction(dataID: Int, title: String, user: String, groupName: String) {
        Column(
                modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgeSuccessSubtle(Modifier.padding(end = 8.dp)) {
                    Text(groupName)
                }
                Text(title, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(user, color = OrbitTheme.colors.primary.normal) // 账号
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                // 编辑按钮
                // Todo 2022.6.23 完善
                ButtonPrimary(onClick = { /*TODO*/ }, content = { Text("编辑") }, modifier = Modifier.padding(end = 8.dp))
                // 删除按钮
                ButtonCritical(onClick = {
                    AppDatabase.database.PasswordDataDao().dropData(dataID)
                    this@PasswordDetailActivity.finish()
                    android.widget.Toast.makeText(this@PasswordDetailActivity, "删除成功", android.widget.Toast.LENGTH_SHORT).show()
                }, content = { Text("删除") })
            }
        }
    }

    /**
     * 详细信息
     */
    @Composable
    fun PassDetail(title: String, user: String, password: String, remark: String) {
        // 密码的显示与隐藏
        var passwordHIDE = ""
        for (item in password) {
            passwordHIDE += "•"
        }
        var passwordShowing by remember { mutableStateOf(passwordHIDE) }
        // 剪切板
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 备注
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(remark, color = OrbitTheme.colors.content.minor, modifier = Modifier.padding(top = 16.dp)) // 备注
        }
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(24.dp),
                        tint = OrbitTheme.colors.content.minor)
                KeyValueLarge(key = "用户名 & 账号", value = user, modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f))
                IconButton(onClick = {
                    val clipData = ClipData.newPlainText("user", user)
                    clipboard.setPrimaryClip(clipData)
                    android.widget.Toast.makeText(this@PasswordDetailActivity, "已复制账号", android.widget.Toast.LENGTH_SHORT).show()
                }) { // 复制按钮
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_content_copy_24), contentDescription = "复制",
                            tint = OrbitTheme.colors.primary.normal)
                }
            }
            Row(modifier = Modifier
                    .padding(top = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                                onPress = { passwordShowing = if (passwordShowing == passwordHIDE) password else passwordHIDE }
                        )
                    }, verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_baseline_key_24), contentDescription = null, modifier = Modifier.size(24.dp),
                        tint = OrbitTheme.colors.content.minor)
                KeyValueLarge(key = "密码 | 轻触以显示", value = passwordShowing, modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f))
                IconButton(onClick = {
                    val clipData = ClipData.newPlainText("password", password)
                    clipboard.setPrimaryClip(clipData)
                    android.widget.Toast.makeText(this@PasswordDetailActivity, "已复制密码", android.widget.Toast.LENGTH_SHORT).show()
                }) { // 复制按钮
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_content_copy_24), contentDescription = "复制",
                            tint = OrbitTheme.colors.primary.normal)
                }
            }
            TextButton(modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                    onClick = {
                        val clipData = ClipData.newPlainText("info", "用途:$title\n用户名: $user\n密码: $password")
                        clipboard.setPrimaryClip(clipData)
                    }) {
                Text("复制用户名和密码")
            }
        }
    }
}