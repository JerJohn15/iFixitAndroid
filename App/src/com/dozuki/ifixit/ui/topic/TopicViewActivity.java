package com.dozuki.ifixit.ui.topic;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.guide.view.FullImageViewActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.ui.topic.adapters.TopicPageAdapter;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class TopicViewActivity extends BaseActivity {
   public static final String TOPIC_KEY = "TOPIC";
   private static final String TOPIC_VIEW_TAG = "TOPIC_VIEW_TAG";

   private TopicViewFragment mTopicView;
   private TopicNode mTopicNode;
   private TopicLeaf mTopic;
   private ImageView mBackdropView;
   private ViewPager mPager;
   private TabLayout mTabs;
   private TopicPageAdapter mPageAdapter;
   private CollapsingToolbarLayout mCollapsingToolbar;

   public static Intent viewTopic(Context context, String topicName) {
      Intent intent = new Intent(context, TopicViewActivity.class);
      intent.putExtra(GuideViewActivity.TOPIC_NAME_KEY, topicName);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setTheme(R.style.Theme_Base_TransparentActionBar);

      setContentView(R.layout.topic_view);

      mContentFrame = (FrameLayout) findViewById(R.id.content_frame);

      mToolbar = (Toolbar) findViewById(R.id.toolbar);
      mToolbar.setTitleTextColor(getResources().getColor(R.color.white));

      setSupportActionBar(mToolbar);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);

      //showLoading(R.id.loading_container);

      mBackdropView = (ImageView) findViewById(R.id.backdrop);
      mPager = (ViewPager) findViewById(R.id.topic_viewpager);
      mTabs = (TabLayout) findViewById(R.id.tabLayout);
      mTabs.setTabGravity(TabLayout.GRAVITY_FILL);
      mTabs.setVisibility(View.VISIBLE);
      mTopicNode = (TopicNode) getIntent().getSerializableExtra(TOPIC_KEY);

      if (mTopicNode != null) {
         String topicName = mTopicNode.getDisplayName();
         mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
         mCollapsingToolbar.setTitle(topicName);
         mCollapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
         App.sendScreenView("/category/" + mTopicNode.getName());

         Api.call(this, ApiCall.topic(topicName));
      }
   }

   private void loadTopicImage() {
      String url = mTopic.getImage().getPath(ImageSizes.topicMain);
      PicassoUtils.with(this)
       .load(url)
       .error(R.drawable.no_image)
       .into(new Target() {
          @Override
          public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
             assert mBackdropView != null;
             mBackdropView.setImageBitmap(bitmap);

             Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                   Palette.Swatch vibrant = palette.getVibrantSwatch();

                   if (vibrant != null) {
                      // If we have a vibrant color
                      // update the title TextView
                      //mCollapsingToolbar.setBackgroundColor(mutedColor);
                      //  mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                      mCollapsingToolbar.setExpandedTitleTextColor(ColorStateList.valueOf(vibrant.getTitleTextColor()));

                      //mCollapsingToolbar.setStatusBarScrimColor(palette.getDarkMutedColor(mutedColor));
                      mCollapsingToolbar.setContentScrimColor(palette.getMutedColor(vibrant.getTitleTextColor()));
                   }
                }
             });

          }

          @Override
          public void onBitmapFailed(Drawable errorDrawable) {

          }

          @Override
          public void onPrepareLoad(Drawable placeHolderDrawable) {

          }
       });

      mBackdropView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String) v.getTag();

            if (url == null || (url.equals("") || url.startsWith("."))) {
               return;
            }

            startActivity(FullImageViewActivity.viewImage(getBaseContext(), url, false));
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         // Respond to the action bar's Up/Home button
         case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Subscribe
   public void onTopic(ApiEvent.Topic event) {
      //hideLoading();
      if (!event.hasError()) {
         mTopic = event.getResult();
         mPageAdapter = new TopicPageAdapter(getSupportFragmentManager(), this, mTopic);
         mPager.setAdapter(mPageAdapter);
         mTabs.setupWithViewPager(mPager);

         loadTopicImage();
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Override
   public void showLoading(int container) {
      findViewById(container).setVisibility(View.VISIBLE);
      super.showLoading(container);
   }

   @Override
   public void hideLoading() {
      super.hideLoading();
      findViewById(R.id.loading_container).setVisibility(View.GONE);
   }
}