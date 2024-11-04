package com.example.videoapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<String> videoUrls;
    private SimpleCache simpleCache;
    private RecyclerView recyclerView;

    public VideoAdapter(Context context, List<String> videoUrls, SimpleCache simpleCache, ViewPager2 viewPager) {
        this.context = context;
        this.videoUrls = videoUrls;
        this.simpleCache = simpleCache;
        this.recyclerView = (RecyclerView) viewPager.getChildAt(0);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
        holder.playerView.setPlayer(player);

        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
        CacheDataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(dataSourceFactory);

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUrls.get(position)));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(false);  // 默认暂停播放
        holder.player = player;
    }

    @Override
    public int getItemCount() {
        return videoUrls.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.player != null) {
            holder.player.setPlayWhenReady(true);  // 当视图可见时播放
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.player != null) {
            holder.player.setPlayWhenReady(false);  // 当视图不可见时暂停
        }
    }

    public void releaseAllPlayers() {
        for (int i = 0; i < videoUrls.size(); i++) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolder instanceof VideoViewHolder) {
                VideoViewHolder holder = (VideoViewHolder) viewHolder;
                if (holder.player != null) {
                    holder.player.release();
                    holder.player = null;
                    holder.playerView.setPlayer(null);
                }
            }
        }
    }

    public void initializePlayers() {
        for (int i = 0; i < videoUrls.size(); i++) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolder instanceof VideoViewHolder) {
                VideoViewHolder holder = (VideoViewHolder) viewHolder;
                if (holder.player == null) {
                    SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
                    holder.playerView.setPlayer(player);

                    DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
                    CacheDataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory()
                            .setCache(simpleCache)
                            .setUpstreamDataSourceFactory(dataSourceFactory);

                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(videoUrls.get(i)));

                    player.setMediaSource(mediaSource);
                    player.prepare();
                    player.setPlayWhenReady(false);
                    holder.player = player;
                }
            }
        }
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        PlayerView playerView;
        ExoPlayer player;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
        }
    }
}