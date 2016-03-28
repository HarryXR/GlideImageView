package com.harry.glideimageview.images.widget;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.harry.glideimageview.R;
import com.harry.glideimageview.images.ImageAdapter;
import com.harry.glideimageview.images.ImageBean;
import com.harry.glideimageview.images.presenter.ImagePresenter;
import com.harry.glideimageview.images.presenter.ImagePresenterImpl;
import com.harry.glideimageview.images.view.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Description :
 * Author : lauren
 * Email  : lauren.liuling@gmail.com
 * Blog   : http://www.liuling123.com
 * Date   : 15/12/22
 */
public class ImageActivity extends Activity implements ImageView, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ImageFragment";

    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ImageAdapter mAdapter;
    private List<ImageBean> mData;
    private ImagePresenter mImagePresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_image);
        mImagePresenter = new ImagePresenterImpl(this);
        mSwipeRefreshWidget = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshWidget.setColorSchemeResources(R.color.primary, R.color.primary_dark, R.color.primary_light,
            R.color.accent);
        mSwipeRefreshWidget.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ImageAdapter(getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        onRefresh();
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        private int lastVisibleItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == mAdapter.getItemCount()) {
                //加载更多
//                Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.image_hit),
// Snackbar.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onRefresh() {
        if (mData != null) {
            mData.clear();
        }
        mImagePresenter.loadImageList();
    }

    public void addImages(List<ImageBean> list) {
        if (mData == null) {
            mData = new ArrayList<ImageBean>();
        }
        mData.addAll(list);
        mAdapter.setmDate(mData);
    }

    @Override
    public void showProgress() {
        mSwipeRefreshWidget.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        mSwipeRefreshWidget.setRefreshing(false);
    }

    @Override
    public void showLoadFailMsg() {
//        View view = getActivity() == null ? mRecyclerView.getRootView() : getActivity().findViewById(R.id
// .drawer_layout);
//        Snackbar.make(view, getString(R.string.load_fail), Snackbar.LENGTH_SHORT).show();
    }
}
