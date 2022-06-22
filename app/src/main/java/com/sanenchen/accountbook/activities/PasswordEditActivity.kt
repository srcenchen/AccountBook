package com.sanenchen.accountbook.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sanenchen.accountbook.R
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.databases.PasswordData
import com.sanenchen.accountbook.databases.PasswordGroup
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.*
import java.util.*

/**
 * 添加密码
 * @author sanenchen
 */
class PasswordEditActivity : ComponentActivity() {
    private lateinit var trySave: MutableState<Boolean>
    private lateinit var selected: MutableState<PasswordGroup>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountBookTheme {
                trySave = remember { mutableStateOf(false) }
                val id = intent.getIntExtra("id", 0)
                val title = remember { mutableStateOf(intent.getStringExtra("title")!!) }
                val user = remember { mutableStateOf(intent.getStringExtra("user")!!) }
                val password = remember { mutableStateOf(intent.getStringExtra("password")!!) }
                val remark = remember { mutableStateOf(intent.getStringExtra("remark")!!) }
                // 分组
                val group = AppDatabase.database.PasswordGroupDao().queryAll()
                val groupList = arrayListOf<PasswordGroup>()
                groupList.add(PasswordGroup(-1, "无分组"))
                for (item in group.collectAsState(listOf()).value) groupList.add(item)
                selected = remember {
                    mutableStateOf(
                        PasswordGroup(
                            intent.getIntExtra("groupID", -1),
                            intent.getStringExtra("groupName")!!
                        )
                    )
                }
                val editMode = intent.getBooleanExtra("editMode", false)
                Scaffold(topBar = {
                    TopAppBar(elevation = 0.dp, title = { Text("编辑密码") }, onNavigateUp = { this.finish() }, actions = {
                        Button(onClick = {
                            trySave.value = true
                            if (title.value.isNotEmpty() && user.value.isNotEmpty() && password.value.isNotEmpty()) {
                                val passwordData = PasswordData(
                                    id, title.value, user.value, password.value, remark.value, 1, selected.value.id
                                )
                                if (!editMode) // 新数据
                                    AppDatabase.database.PasswordDataDao().insertPasswordData(passwordData) // 放入数据
                                else AppDatabase.database.PasswordDataDao().update(passwordData) // 已有数据编辑模式 更新数据
                                this@PasswordEditActivity.finish()
                            }
                        }, content = { Text("保存") }, modifier = Modifier.padding(end = 8.dp))
                    })
                }) {
                    Column(Modifier.padding(it)) {
                        MainUI(title, user, password, remark, selected, groupList)
                    }
                }
            }
        }
    }

    /**
     * 主UI
     */
    @Composable
    fun MainUI(
        title: MutableState<String>, user: MutableState<String>, password: MutableState<String>, remark: MutableState<String>,
        selected: MutableState<PasswordGroup>, groupList: List<PasswordGroup>
    ) {
        val showingMakePassword = remember { mutableStateOf(false) }
        TextField(value = title.value,
            onValueChange = { title.value = it },
            label = { Text("账号名称") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            error = if (title.value.isEmpty() && trySave.value) {
                { Text("此处不能为空") }
            } else null)
        // 阴影
        Spacer(
            modifier = Modifier
                .zIndex(0.5f)
                .shadow(1.dp)
                .fillMaxWidth()
                .height(1.dp)
        )
        Column(
            Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            TextField(value = user.value,
                onValueChange = { user.value = it },
                leadingIcon = { Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = "账号") },
                label = { Text("用户名") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                error = if (user.value.isEmpty() && trySave.value) {
                    { Text("此处不能为空") }
                } else null)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                TextField(value = password.value,
                    onValueChange = { password.value = it },
                    leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_key_24), contentDescription = "密码") },
                    label = { Text("密码") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    error = if (password.value.isEmpty() && trySave.value) {
                        { Text("此处不能为空") }
                    } else null)
                ButtonPrimarySubtle(
                    onClick = { showingMakePassword.value = true },
                    content = { Text("生成密码") },
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
            TextField(
                value = remark.value,
                onValueChange = { remark.value = it },
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_contact_page_24), contentDescription = null) },
                label = { Text("备注") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                SelectField(modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f),
                    value = selected.value.groupName,
                    options = groupList,
                    onOptionSelect = { selected.value = it },
                    placeholder = { Text("无分组") },
                    label = { Text("分组") }) {
                    Text(it.groupName)
                }
                ButtonPrimarySubtle(
                    onClick = { startActivity(Intent(this@PasswordEditActivity, GroupManagerActivity::class.java)) },
                    content = { Text("管理分组") },
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        // 相关 Alert 展示
        if (showingMakePassword.value)
            MakePassword(showing = showingMakePassword, password = password)
    }

    /**
     * 判断所选分组是否被删除
     */
    override fun onResume() {
        super.onResume()
        // 将ID扔进数据库查询，如果没有这个分组，就赶紧删掉
        try { // 数据可能没初始化，问题不大
            if (AppDatabase.database.PasswordGroupDao().queryWithID(selected.value.id).isEmpty())
                selected.value = PasswordGroup(-1, "无分组")
        } catch (e: Exception) {
        }
    }

    /**
     * 生成密码
     */
    @Composable
    fun MakePassword(showing: MutableState<Boolean>, password: MutableState<String>) {
        var sliderPosition by remember { mutableStateOf(15f) }
        var randPassword by remember { mutableStateOf(getRandPassword(sliderPosition.toInt())) }
        AlertDialog(
            onDismissRequest = { showing.value = false },
            title = { Text("生成密码") },
            text = {
                Column {
                    Text("长度：${sliderPosition.toInt()}")
                    Slider(
                        steps = 12,
                        valueRange = 8f..20f,
                        onValueChange = { sliderPosition = it; randPassword = getRandPassword(sliderPosition.toInt()) },
                        value = sliderPosition
                    )

                    Text("生成的密码：\n$randPassword", style = OrbitTheme.typography.title3)
                }

            },
            dismissButton = { TextButton(onClick = { showing.value = false }, content = { Text("取消") }) },
            confirmButton = { TextButton(onClick = { password.value = randPassword; showing.value = false }, content = { Text("确认") }) }
        )
    }

    /**
     * 随机生成密码
     */
    private fun getRandPassword(n: Int): String {
        val characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val random = Random(System.nanoTime())
        val password = StringBuilder()

        for (i in 0 until n) {
            val rIndex = random.nextInt(characterSet.length)
            password.append(characterSet[rIndex])
        }

        return password.toString()
    }
}