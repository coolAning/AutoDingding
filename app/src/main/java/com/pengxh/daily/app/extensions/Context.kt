package com.pengxh.daily.app.extensions

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.pengxh.daily.app.service.FloatingWindowService
import com.pengxh.daily.app.ui.MainActivity
import com.pengxh.daily.app.utils.Constant
import com.pengxh.daily.app.utils.MessageEvent
import com.pengxh.kt.lite.utils.RetrofitFactory.kTag
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog
import org.greenrobot.eventbus.EventBus

/**
 * 检测通知监听服务是否被授权
 * */
fun Context.notificationEnable(): Boolean {
    val packages = NotificationManagerCompat.getEnabledListenerPackages(this)
    return packages.contains(this.packageName)
}

/**
 * 打开指定包名的apk
 */
fun Context.openApplication(packageName: String, needEmail: Boolean) {
    val pm = this.packageManager
    val isContains = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            pm.getPackageInfo(packageName, 0)
        }
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
    if (!isContains) {
        AlertMessageDialog.Builder()
            .setContext(this)
            .setTitle("温馨提醒")
            .setMessage("手机没有安装《钉钉》软件，无法自动打卡")
            .setPositiveButton("知道了")
            .setOnDialogButtonClickListener(object :
                AlertMessageDialog.OnDialogButtonClickListener {
                override fun onConfirmClick() {

                }
            }).build().show()
        return
    }
    FloatingWindowService.weakReferenceHandler?.apply {
        sendEmptyMessage(Constant.SHOW_FLOATING_WINDOW_CODE)
    }
    /***跳转钉钉开始*****************************************/
    val resolveIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        setPackage(packageName)
    }
    val apps = pm.queryIntentActivities(resolveIntent, 0)
    //前面已经判断过钉钉是否安装，所以此处一定有值
    val info = apps.first()
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        component = ComponentName(info.activityInfo.packageName, info.activityInfo.name)
    }
    this.startActivity(intent)
    /***跳转钉钉结束*****************************************/
    if (needEmail) {
        EventBus.getDefault().post(MessageEvent(Constant.START_COUNT_DOWN_TIMER_CODE))
    }
}

fun Context.backToMainActivity() {
    // 取消倒计时（保留原有功能）
    EventBus.getDefault().post(MessageEvent(Constant.CANCEL_COUNT_DOWN_TIMER_CODE))
    
    // 创建返回到MainActivity的Intent
    val intent = Intent(this, MainActivity::class.java).apply {
        // 以下标志组合能更有效地将应用带到前台
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)       // 在新任务中启动
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)      // 清除目标活动上方的所有活动
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)     // 如果已经运行就不创建新实例
        
        // 传递额外信息，告知MainActivity是从自动任务返回的
        putExtra("from_auto_task", true)
    }
    
    // 检查设置是否需要优先返回主屏幕
    if (SaveKeyValues.getValue(Constant.BACK_TO_HOME, false) as Boolean) {
        // 使用Handler延迟执行，避免主线程阻塞
        android.os.Handler().postDelayed({
            this.startActivity(intent)
        }, 500)
        
        // 先回到主屏幕
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        this.startActivity(homeIntent)
    } else {
        // 直接启动MainActivity
        this.startActivity(intent)
    }

    // 对于高版本Android，可以使用以下方式提高任务切换的成功率
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        try {
            // 请求系统将此应用带到前台，仅在Android 9.0+可用
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.appTasks.firstOrNull()?.moveToFront()
        } catch (e: Exception) {
            Log.e(kTag, "无法将应用移至前台: ${e.message}")
        }
    }
}