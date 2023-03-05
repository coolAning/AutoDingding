package com.pengxh.autodingding.fragment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import cn.bertsir.zbar.utils.QRUtils
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.R
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao
import com.pengxh.autodingding.ui.HistoryRecordActivity
import com.pengxh.autodingding.ui.NoticeRecordActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : KotlinBaseFragment() {

    private var historyBeanDao: HistoryRecordBeanDao? = null
    private var notificationManager: NotificationManager? = null

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initLayoutView(): Int = R.layout.fragment_settings

    override fun initData() {
        historyBeanDao = BaseApplication.get().daoSession.historyRecordBeanDao
        notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
        if (!TextUtils.isEmpty(emailAddress)) {
            emailTextView.text = emailAddress
        }
        appVersion.text = BuildConfig.VERSION_NAME
    }

    override fun initEvent() {
        emailLayout.setOnClickListener { v ->
            AlertInputDialog.Builder()
                .setContext(requireContext())
                .setTitle("设置邮箱")
                .setHintMessage("请输入邮箱")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogButtonClickListener(object :
                    AlertInputDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick(value: String) {
                        if (!TextUtils.isEmpty(value)) {
                            SaveKeyValues.putValue(Constant.EMAIL_ADDRESS, value)
                            emailTextView.text = value
                        } else {
                            "什么都还没输入呢！".show(requireContext())
                        }
                    }

                    override fun onCancelClick() {}
                }).build().show()
        }

        historyLayout.setOnClickListener {
            requireContext().navigatePageTo<HistoryRecordActivity>()
        }

        notificationLayout.setOnClickListener {
            requireContext().navigatePageTo<NoticeRecordActivity>()
        }

        introduceLayout.setOnClickListener { v ->
            AlertMessageDialog.Builder()
                .setContext(requireContext())
                .setTitle("功能介绍")
                .setMessage(requireContext().getString(R.string.about))
                .setPositiveButton("看完了")
                .setOnDialogButtonClickListener(
                    object : AlertMessageDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {

                        }
                    }
                ).build().show()
        }
        if (!notificationEnable()) {
            try {
                //打开通知监听设置页面
                requireContext().startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            //创建常住通知栏
            createNotification()
        }

        //先识别出来备用
        try {
            val codeValue: String = QRUtils.getInstance().decodeQRcode(updateCodeView)
            SaveKeyValues.putValue("updateLink", codeValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateCodeView.setOnLongClickListener { v ->
            val updateLink =
                SaveKeyValues.getValue("updateLink", "https://www.pgyer.com/MBGt") as String
            AlertMessageDialog.Builder()
                .setContext(requireContext())
                .setTitle("识别结果")
                .setMessage(updateLink)
                .setPositiveButton("前往更新页面(密码：123)")
                .setOnDialogButtonClickListener(object :
                    AlertMessageDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick() {
                        val intent = Intent()
                        intent.action = "android.intent.action.VIEW"
                        intent.data = Uri.parse(updateLink)
                        startActivity(intent)
                    }
                }).build().show()
            true
        }
    }

    /**
     * 每次切换到此页面都需要重新计算记录
     */
    override fun onResume() {
        super.onResume()
        recordSize.text = historyBeanDao?.loadAll()?.size.toString()
        noticeCheckBox.isChecked = notificationEnable()
        super.onResume()
    }

    //检测通知监听服务是否被授权
    private fun notificationEnable(): Boolean {
        val packages: Set<String> =
            NotificationManagerCompat.getEnabledListenerPackages(requireContext())
        //        for (String aPackage : packages) {
//            Log.d(TAG, "notificationEnable ===> "+aPackage);
//        }
        return packages.contains(requireContext().packageName)
    }

    private fun createNotification() {
        //Android8.0以上必须添加 渠道 才能显示通知栏
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            val name: String = requireContext().resources.getString(R.string.app_name)
            val id = name + "_DefaultNotificationChannel"
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.setShowBadge(true)
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC //设置锁屏可见
            notificationManager?.createNotificationChannel(mChannel)
            Notification.Builder(requireContext(), id)
        } else {
            Notification.Builder(requireContext())
        }
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(requireContext().resources, R.mipmap.logo_round)
        builder.setContentTitle("钉钉打卡通知监听已打开")
            .setContentText("如果通知消失，请重新开启应用")
            .setWhen(System.currentTimeMillis())
            .setLargeIcon(bitmap)
            .setSmallIcon(R.mipmap.logo_round)
            .setAutoCancel(false)
        val notification = builder.build()
        notification.flags = Notification.FLAG_NO_CLEAR
        notificationManager?.notify(Int.MAX_VALUE, notification)
    }
}