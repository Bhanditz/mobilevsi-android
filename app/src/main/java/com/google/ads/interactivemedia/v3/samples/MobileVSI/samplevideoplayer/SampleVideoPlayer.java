/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ads.interactivemedia.v3.samples.MobileVSI.samplevideoplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * A VideoView that intercepts various methods and reports them back via a PlayerCallback.
 */
public class SampleVideoPlayer extends VideoView implements VideoPlayer {

    private enum PlaybackState {
        STOPPED, PAUSED, PLAYING
    }

    private MediaController mMediaController;
    private PlaybackState mPlaybackState;
    private final List<PlayerCallback> mVideoPlayerCallbacks = new ArrayList<PlayerCallback>(1);

    public SampleVideoPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SampleVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SampleVideoPlayer(Context context) {
        super(context);
        init();
    }

    private void init() {
        mPlaybackState = PlaybackState.STOPPED;
        mMediaController = new MediaController(getContext());
        mMediaController.setAnchorView(this);
        enablePlaybackControls();

        // Set OnCompletionListener to notify our callbacks when the video is completed.
        super.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Reset the MediaPlayer.
                // This prevents a race condition which occasionally results in the media
                // player crashing when switching between videos.
                disablePlaybackControls();
                mediaPlayer.reset();
                mediaPlayer.setDisplay(getHolder());
                enablePlaybackControls();
                mPlaybackState = PlaybackState.STOPPED;

                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onCompleted();
                }
            }
        });

        // Set OnErrorListener to notify our callbacks if the video errors.
        super.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mPlaybackState = PlaybackState.STOPPED;
                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onError();
                }

                // Returning true signals to MediaPlayer that we handled the error. This will
                // prevent the completion handler from being called.
                return true;
            }
        });
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        // The OnCompletionListener can only be implemented by SampleVideoPlayer.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        // The OnErrorListener can only be implemented by SampleVideoPlayer.
        throw new UnsupportedOperationException();
    }

    // Methods implementing the VideoPlayer interface.
    @Override
    public void play() {
        start();
    }

    @Override
    public void start() {
        super.start();
        PlaybackState oldPlaybackState = mPlaybackState;
        mPlaybackState = PlaybackState.PLAYING;
        switch (oldPlaybackState) {
            case STOPPED:
                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onPlay();
                }
                break;
            case PAUSED:
                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onResume();
                }
                break;
            default:
                // Already playing; do nothing.
                break;
        }
    }

    @Override
    public void pause() {
        super.pause();
        mPlaybackState = PlaybackState.PAUSED;
        for (PlayerCallback callback : mVideoPlayerCallbacks) {
            callback.onPause();
        }
    }

    @Override
    public void stopPlayback() {
        super.stopPlayback();
        mPlaybackState = PlaybackState.STOPPED;
    }

    @Override
    public void disablePlaybackControls() {
        setMediaController(null);
    }

    @Override
    public void enablePlaybackControls() {
        setMediaController(mMediaController);
    }

    @Override
    public void addPlayerCallback(PlayerCallback callback) {
        mVideoPlayerCallbacks.add(callback);
    }

    @Override
    public void removePlayerCallback(PlayerCallback callback) {
        mVideoPlayerCallbacks.remove(callback);
    }
}
