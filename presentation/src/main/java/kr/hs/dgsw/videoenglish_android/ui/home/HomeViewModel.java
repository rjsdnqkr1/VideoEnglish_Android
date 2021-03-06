package kr.hs.dgsw.videoenglish_android.ui.home;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import kr.hs.dgsw.data.util.Constants;
import kr.hs.dgsw.domain.model.YoutubeData;
import kr.hs.dgsw.domain.usecase.hiding.InsertHidingUseCase;
import kr.hs.dgsw.domain.usecase.playlist.GetDefaultPlaylistListUseCase;
import kr.hs.dgsw.domain.usecase.playlist.GetPlaylistListUseCase;
import kr.hs.dgsw.videoenglish_android.base.viewmodel.BaseViewModel;
import kr.hs.dgsw.videoenglish_android.widget.SingleLiveEvent;
import kr.hs.dgsw.videoenglish_android.widget.recyclerview.video.VideoListAdapter;
import kr.hs.dgsw.videoenglish_android.widget.recyclerview.video.VideoViewType;

public class HomeViewModel extends BaseViewModel {

    private GetDefaultPlaylistListUseCase getDefaultPlaylistListUseCase;
    private InsertHidingUseCase insertHidingUseCase;

    public HomeViewModel(GetDefaultPlaylistListUseCase getDefaultPlaylistListUseCase,
                         InsertHidingUseCase insertHidingUseCase) {
        this.getDefaultPlaylistListUseCase = getDefaultPlaylistListUseCase;
        this.insertHidingUseCase = insertHidingUseCase;
    }

    List<YoutubeData> videoList = new ArrayList<>();
    public VideoListAdapter videoListAdapter = new VideoListAdapter(videoList, VideoViewType.VERTICAL_NORMAL);

    private SingleLiveEvent onSuccessHidingEvent = new SingleLiveEvent<>();
    LiveData getOnSuccessHidingEvent() {
        return onSuccessHidingEvent;
    }

    void setYoutubeDataList() {
        addDisposable(getDefaultPlaylistListUseCase.buildUseCaseObservable(),
                new DisposableSingleObserver<List<YoutubeData>>() {
                    @Override
                    public void onSuccess(List<YoutubeData> youtubeDataList) {
                        HomeViewModel.this.videoList.clear();
                        HomeViewModel.this.videoList.addAll(youtubeDataList);
                        videoListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        setOnErrorEvent(e);
                    }
                });
    }

    void insertHiding(YoutubeData video) {
        addDisposable(insertHidingUseCase.buildUseCaseObservable(new InsertHidingUseCase.Params(video)),
                new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        int position = videoList.indexOf(video);
                        videoList.remove(video);
                        videoListAdapter.notifyItemRemoved(position);
                        onSuccessHidingEvent.call();
                    }

                    @Override
                    public void onError(Throwable e) {
                        setOnErrorEvent(e);
                    }
                });
    }
}
