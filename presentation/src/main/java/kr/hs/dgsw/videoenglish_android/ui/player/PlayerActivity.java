package kr.hs.dgsw.videoenglish_android.ui.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import kr.co.prnd.YouTubePlayerView;
import kr.hs.dgsw.domain.model.YoutubeData;
import kr.hs.dgsw.videoenglish_android.R;
import kr.hs.dgsw.videoenglish_android.base.BaseActivity;
import kr.hs.dgsw.videoenglish_android.databinding.ActivityPlayerBinding;
import kr.hs.dgsw.videoenglish_android.ui.favorites.FavoritesBottomSheetDialog;

public class PlayerActivity extends BaseActivity<ActivityPlayerBinding, PlayerViewModel> {

    @Inject
    PlayerViewModelFactory viewModelFactory;

    public static final String EXTRA_VIDEO = "video";
    public static final String EXTRA_VIDEO_LIST = "videoList";

    @NotNull
    @Override
    protected PlayerViewModel getViewModel() {
        return new ViewModelProvider(this, viewModelFactory).get(PlayerViewModel.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void observerViewModel() {
        mViewModel.getOnEmptyWordEvent().observe(this, o ->
                Toast.makeText(this, R.string.error_empty, Toast.LENGTH_SHORT).show());

        mViewModel.videoListAdapter.getOnClickItemEvent().observe(this, youtubeData ->
                startActivity(
                        new Intent(getApplicationContext(), PlayerActivity.class)
                                .putExtra(PlayerActivity.EXTRA_VIDEO, youtubeData)
                                .putExtra(PlayerActivity.EXTRA_VIDEO_LIST, (Serializable) mViewModel.getVideoList())
                )
        );

        mViewModel.videoListAdapter.getOnAddFavoritesEvent().observe(this, youtubeData -> {
            new FavoritesBottomSheetDialog(youtubeData).show(getSupportFragmentManager());
        });

        mViewModel.videoListAdapter.getOnOpenYoutubeEvent().observe(this, this::openYoutube);

        mViewModel.videoListAdapter.getOnShareEvent().observe(this, this::share);

        mViewModel.videoListAdapter.getOnHideEvent().observe(this, youtubeData -> mViewModel.insertHiding(youtubeData));

        mViewModel.getOnSuccessHidingEvent().observe(this, o -> Toast.makeText(this, R.string.message_hiding, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initIntent();
        clickEvent();
    }

    @SuppressWarnings("unchecked")
    private void initIntent() {
        Serializable video = getIntent().getSerializableExtra(EXTRA_VIDEO);
        if (video == null) {
            finish();
            return;
        }
        mViewModel.setVideo((YoutubeData) video);
        initVideo();
        mViewModel.insertResent();
        Serializable videoList = getIntent().getSerializableExtra(EXTRA_VIDEO_LIST);
        if (videoList == null) {
            finish();
            return;
        }
        mViewModel.setVideoList((ArrayList<YoutubeData>) videoList);
    }

    private void initVideo() {
        mBinding.youtubePlayerView.play(Objects.requireNonNull(mViewModel.video.getVideoId()), new YouTubePlayerView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(@NotNull YouTubePlayer.Provider provider, @NotNull YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(mViewModel.video.getVideoId());
            }

            @Override
            public void onInitializationFailure(@NotNull YouTubePlayer.Provider provider, @NotNull YouTubeInitializationResult youTubeInitializationResult) { }
        });
    }

    private void clickEvent() {
        mBinding.btnOpenYoutube.setOnClickListener(v -> openYoutube(mViewModel.video));
        mBinding.btnShare.setOnClickListener(v -> share(mViewModel.video));
        mBinding.btnAddFavorites.setOnClickListener(v -> new FavoritesBottomSheetDialog(mViewModel.video).show(getSupportFragmentManager()));
    }

    private void openYoutube(YoutubeData youtubeData) {
        startActivity(
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://youtube.com/watch?v=" + youtubeData.getVideoId())
                )
        );
    }

    private void share(YoutubeData youtubeData) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "http://youtube.com/watch?v=" + youtubeData.getVideoId());
        startActivity(Intent.createChooser(intent, "싱생송 - 무료 노래방"));
    }
}
