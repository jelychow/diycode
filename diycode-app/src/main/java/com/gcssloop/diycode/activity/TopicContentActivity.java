/*
 * Copyright 2017 GcsSloop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 2017-03-08 04:22:13
 *
 * GitHub:  https://github.com/GcsSloop
 * Website: http://www.gcssloop.com
 * Weibo:   http://weibo.com/GcsSloop
 */

package com.gcssloop.diycode.activity;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gcssloop.diycode.R;
import com.gcssloop.diycode.adapter.TopicReplyListAdapter;
import com.gcssloop.diycode.base.BaseActivity;
import com.gcssloop.diycode.base.ViewHolder;
import com.gcssloop.diycode.base.adapter.GcsViewHolder;
import com.gcssloop.diycode.utils.RecyclerViewUtil;
import com.gcssloop.diycode.widget.MarkdownView;
import com.gcssloop.diycode_sdk.api.topic.bean.Topic;
import com.gcssloop.diycode_sdk.api.topic.bean.TopicReply;
import com.gcssloop.diycode_sdk.api.topic.event.GetTopicEvent;
import com.gcssloop.diycode_sdk.api.topic.event.GetTopicRepliesListEvent;
import com.gcssloop.diycode_sdk.api.user.bean.User;
import com.gcssloop.diycode_sdk.utils.TimeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.gcssloop.diycode.R.id.reply_list;

public class TopicContentActivity extends BaseActivity implements View.OnClickListener {
    public static String TOPIC = "topic";
    private TopicReplyListAdapter mAdapter;
    private Topic topic;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_topic_content;
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        initTopicContentPanel(holder);
        initRecyclerView(holder);
    }

    // 初始化 topic 内容面板的数据
    private void initTopicContentPanel(ViewHolder holder) {
        Intent intent = getIntent();
        topic = (Topic) intent.getSerializableExtra(TOPIC);
        if (null != topic) {
            toastShort("获取 topic 成功");
            User user = topic.getUser();
            holder.setText(R.id.username, user.getLogin() + "(" + user.getName() + ")");
            holder.setText(R.id.time, TimeUtil.computePastTime(topic.getUpdated_at()));
            holder.setText(R.id.title, topic.getTitle());
            holder.loadImage(this, user.getAvatar_url(), R.id.avatar);
            holder.setOnClickListener(this, R.id.avatar, R.id.username);

            // 发出获取 topic 详情和 topic 回复的请求
            // TODO 分页加载回复的内容(鉴于目前回复数量并不多，此处采取一次性加载)
            mDiycode.getTopic(topic.getId());
            mDiycode.getTopicRepliesList(topic.getId(), null, topic.getReplies_count());
        } else {
            toastShort("获取 topic 失败");
        }
    }

    private void initRecyclerView(ViewHolder holder) {
        mAdapter = new TopicReplyListAdapter(this) {
            @Override
            public void setListener(int position, GcsViewHolder holder, TopicReply topic) {
                super.setListener(position, holder, topic);
                // TODO 此处设置监听器
            }
        };

        RecyclerView recyclerView = holder.get(reply_list);
        RecyclerViewUtil.init(this, recyclerView, mAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTopicDetail(GetTopicEvent event) {
        if (event.isOk()) {
            MarkdownView markdownView = mViewHolder.get(R.id.content);
            markdownView.setMarkDownText(event.getBean().getBody());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTopicReplysList(GetTopicRepliesListEvent event) {
        if (event.isOk()) {
            List<TopicReply> replies = event.getBean();
            mAdapter.addDatas(replies);
        } else {
            toastShort("获取回复失败");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar:
            case R.id.username:
                if (null != topic) {
                    openActivity(UserActivity.class, UserActivity.USER, topic.getUser());
                }
                break;
        }
    }
}