package com.sanenchen.accountbook.activities

import android.app.Activity
import android.os.Bundle
import com.sanenchen.accountbook.R
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
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
                    SettingsComponents(activity = this, it)
                }
            }
        }
    }

    @Composable
    fun SettingsComponents(activity: Activity, paddingValues: PaddingValues) {
        val mainInformation = AppDatabase.database.SettingInformationDao().queryAll()[0]
        val isUsingBiometric = remember { mutableStateOf(mainInformation.isUsingBiometric) }
        val allowScreenShot = remember { mutableStateOf(mainInformation.allowScreenShot) }
        Card(modifier = Modifier
            .padding(all = 16.dp)
            .padding(paddingValues)) {
            Column(Modifier.padding(all = 16.dp)) {
                Text(text = "设置", modifier = Modifier.padding(bottom = 16.dp))
                SettingItem(
                    icon = painterResource(id = R.drawable.baseline_fingerprint_24),
                    text = "生物信息验证",
                    isUsed = isUsingBiometric,
                    enabled = isBiometric(activity)
                ) {
                    mainInformation.isUsingBiometric = isUsingBiometric.value
                    AppDatabase.database.SettingInformationDao().update(mainInformation)
                }
                Spacer(modifier = Modifier.padding(top = 16.dp))
                SettingItem(
                    icon = painterResource(id = R.drawable.baseline_screenshot_24),
                    text = "允许在主界面截屏",
                    isUsed = allowScreenShot,
                    enabled = true
                ) {
                    mainInformation.allowScreenShot = allowScreenShot.value
                    AppDatabase.database.SettingInformationDao().update(mainInformation)
                    // 截图状态更新
                    if (!allowScreenShot.value)
                        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) // 禁止截图
                    else
                        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) // 允许截图
                }
            }
        }
    }

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
     * 判断是否支持生物认证
     */
    private fun isBiometric(activity: Activity): Boolean {
        return BiometricManager.from(activity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }
}