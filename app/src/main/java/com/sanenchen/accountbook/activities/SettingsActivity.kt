package com.sanenchen.accountbook.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sanenchen.accountbook.R
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.ui.controls.*

/**
 * 设置Activity
 * @author sanenchen
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountBookTheme {
                Scaffold(topBar = {
                    TopAppBar(title = { Text("设置") }, onNavigateUp = { this.finish() })
                }) {
                    Column(modifier = Modifier.padding(it)) {
                        SettingsComponents()
                        ToolBoxComponents()
                    }
                }
            }
        }
    }

    /**
     * 设置总卡片
     */
    @Composable
    fun SettingsComponents() {
        val mainInformation = AppDatabase.database.SettingInformationDao().queryAll()[0]
        val isUsingBiometric = remember { mutableStateOf(mainInformation.isUsingBiometric) }
        val allowScreenShot = remember { mutableStateOf(mainInformation.allowScreenShot) }
        Card(
            modifier = Modifier
                .padding(all = 16.dp)
        ) {
            Column(Modifier.padding(all = 16.dp)) {
                Text(text = "设置", modifier = Modifier.padding(bottom = 16.dp))
                SettingItem(
                    icon = painterResource(id = R.drawable.baseline_fingerprint_24),
                    text = "生物信息验证",
                    isUsed = isUsingBiometric,
                    enabled = isBiometric(this@SettingsActivity)
                ) {
                    mainInformation.isUsingBiometric = isUsingBiometric.value
                    AppDatabase.database.SettingInformationDao().update(mainInformation)
                }
                Spacer(modifier = Modifier.padding(top = 16.dp))
                SettingItem(
                    icon = painterResource(id = R.drawable.baseline_screenshot_24),
                    text = "允许在私密界面截屏",
                    isUsed = allowScreenShot,
                    enabled = true
                ) {
                    mainInformation.allowScreenShot = allowScreenShot.value
                    AppDatabase.database.SettingInformationDao().update(mainInformation)
                }
            }
        }
    }

    /**
     * 设置项
     */
    @Composable
    fun SettingItem(icon: Painter, text: String, isUsed: MutableState<Boolean>, enabled: Boolean, callback: () -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(painter = icon, contentDescription = null, Modifier.size(24.dp))
            Text(
                text, modifier = Modifier
                    .padding(start = 32.dp)
                    .weight(1f)
            )
            Switch(checked = isUsed.value, onCheckedChange = { isUsed.value = it; callback() }, enabled = enabled)
        }
    }

    /**
     * 工具箱总卡片
     */
    @Composable
    fun ToolBoxComponents() {
        val changingSafePassword = remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Column {
                Text(text = "工具箱", modifier = Modifier.padding(start = 16.dp, top = 16.dp))
                ToolItem(painterResource(id = R.drawable.ic_baseline_groups_24), "密码分组管理") {
                    startActivity(
                        Intent(
                            this@SettingsActivity,
                            GroupManagerActivity::class.java
                        )
                    )
                }
                ToolItem(kiwi.orbit.compose.icons.Icons.Security, "安全密码修改") {
                    changingSafePassword.value = true
                }
            }
        }
        if (changingSafePassword.value) // 修改安全密码
            ChangeSafePassword(changingSafePassword)
    }

    /**
     * 工具项
     */
    @Composable
    fun ToolItem(icon: Painter, text: String, callback: () -> Unit) {
        Box(Modifier.clickable { callback() }) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Icon(painter = icon, contentDescription = null, Modifier.size(24.dp))
                Text(
                    text, modifier = Modifier
                        .padding(start = 32.dp)
                        .weight(1f)
                )
            }
        }
    }

    /**
     * 修改安全密码
     */
    @Composable
    fun ChangeSafePassword(changingSafePassword: MutableState<Boolean>) {
        val settingInformation = AppDatabase.database.SettingInformationDao().queryAll()[0]
        val oldVerifyPassword = settingInformation.password
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var newPasswordAgain by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { changingSafePassword.value = false },
            title = { Text("修改安全密码") },
            text = {
                Column {
                    TextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("旧的安全密码") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        error = if (oldPassword != oldVerifyPassword && oldPassword.isNotEmpty()) {
                            { Text("旧的安全密码错误！") }
                        } else null
                    )
                    TextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("新的安全密码") },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        error = if (newPassword.length < 6 && newPassword.isNotEmpty()) {
                            { Text("安全密码至少需要6位数字") }
                        } else null,
                        info = { Text("安全密码至少需要6位数字") }
                    )
                    TextField(
                        value = newPasswordAgain,
                        onValueChange = { newPasswordAgain = it },
                        label = { Text("再来一次") },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        error = if (newPasswordAgain != newPassword && newPasswordAgain.isNotEmpty()) {
                            { Text("新旧密码不一致") }
                        } else null
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (oldPassword == oldVerifyPassword && oldPassword.isNotEmpty() && // 安全验证
                        newPassword.length >= 6 && newPassword.isNotEmpty() &&
                        newPasswordAgain == newPassword && newPasswordAgain.isNotEmpty()
                    ) {
                        settingInformation.password = newPasswordAgain
                        AppDatabase.database.SettingInformationDao().update(settingInformation) // 更新密码
                        changingSafePassword.value = false
                        android.widget.Toast.makeText(this@SettingsActivity, "密码更新成功", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }, content = { Text("好了") })
            },
            dismissButton = {
                TextButton(onClick = { changingSafePassword.value = false }, content = { Text("取消") })
            }
        )
    }

    /**
     * 判断是否支持生物认证
     */
    private fun isBiometric(activity: Activity): Boolean {
        return BiometricManager.from(activity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }
}