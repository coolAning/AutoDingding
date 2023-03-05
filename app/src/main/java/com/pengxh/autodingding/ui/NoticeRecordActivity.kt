package com.pengxh.autodingding.ui

import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.view.View
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.NotificationBean
import com.pengxh.autodingding.greendao.DaoSession
import com.pengxh.kt.lite.adapter.NormalRecyclerAdapter
import com.pengxh.kt.lite.adapter.ViewHolder
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.utils.ImmerseStatusBarUtil
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import kotlinx.android.synthetic.main.activity_notice.*

class NoticeRecordActivity : KotlinBaseActivity() {

    private lateinit var weakReferenceHandler: WeakReferenceHandler
    private lateinit var daoSession: DaoSession
    private lateinit var noticeAdapter: NormalRecyclerAdapter<NotificationBean>
    private var dataBeans: MutableList<NotificationBean> = ArrayList()
    private var isRefresh = false
    private var isLoadMore = false
    private var offset = 0 // 本地数据库分页从0开始

    override fun setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(
            this, R.color.colorAppThemeLight.convertColor(this)
        )
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        titleView.text = "所有通知"
    }

    override fun initData() {
        weakReferenceHandler = WeakReferenceHandler(callback)
        daoSession = BaseApplication.get().daoSession
        dataBeans = queryNotificationRecord()
        weakReferenceHandler.sendEmptyMessage(2022061901)
    }

    override fun initEvent() {
        refreshLayout.setOnRefreshListener { refreshLayout ->
            isRefresh = true
            object : CountDownTimer(1000, 500) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    isRefresh = false
                    dataBeans.clear()
                    offset = 0
                    dataBeans = queryNotificationRecord()
                    refreshLayout.finishRefresh()
                    weakReferenceHandler.sendEmptyMessage(2022061901)
                }
            }.start()
        }

        refreshLayout.setOnLoadMoreListener { refreshLayout ->
            isLoadMore = true
            object : CountDownTimer(1000, 500) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    isLoadMore = false
                    offset++
                    dataBeans.addAll(queryNotificationRecord())
                    refreshLayout.finishLoadMore()
                    weakReferenceHandler.sendEmptyMessage(2022061901)
                }
            }.start()
        }
    }

    override fun initLayoutView(): Int = R.layout.activity_notice

    override fun observeRequestState() {

    }

    private val callback = Handler.Callback { msg: Message ->
        if (msg.what == 2022061901) {
            if (isRefresh || isLoadMore) {
                noticeAdapter.notifyDataSetChanged()
            } else { //首次加载数据
                if (dataBeans.size == 0) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    emptyView.visibility = View.GONE
                    noticeAdapter =
                        object : NormalRecyclerAdapter<NotificationBean>(
                            R.layout.item_list_notice, dataBeans
                        ) {
                            override fun convertView(
                                viewHolder: ViewHolder,
                                position: Int,
                                item: NotificationBean
                            ) {
                                viewHolder.setText(R.id.titleView, "标题：${item.notificationTitle}")
                                    .setText(R.id.packageNameView, "包名：${item.packageName}")
                                    .setText(R.id.messageView, "内容：${item.notificationMsg}")
                                    .setText(R.id.postTimeView, item.postTime)
                            }
                        }
                    notificationView.adapter = noticeAdapter
                }
            }
        }
        true
    }

    private fun queryNotificationRecord(): MutableList<NotificationBean> {
        return daoSession.queryBuilder(NotificationBean::class.java)
            .offset(offset * 15).limit(15).list()
    }
}