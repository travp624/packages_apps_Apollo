/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andrew.apollo.app.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import com.andrew.apollo.R;
import com.andrew.apollo.activities.AudioPlayerHolder;
import com.andrew.apollo.activities.MusicLibrary;
import com.andrew.apollo.service.ApolloService;
import com.andrew.apollo.utils.ApolloUtils;
import com.androidquery.AQuery;

import static com.andrew.apollo.Constants.ALBUM_IMAGE;

/**
 * Simple widget to show currently playing album art along with play/pause and
 * next track buttons.
 */
public class AppWidget42Inverted extends AppWidgetProvider {

    public static final String CMDAPPWIDGETUPDATE = "appwidgetupdate4x2_inverted";

    private static AppWidget42Inverted sInstance;

    public static synchronized AppWidget42Inverted getInstance() {
        if (sInstance == null) {
            sInstance = new AppWidget42Inverted();
        }
        return sInstance;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);

        // Send broadcast intent to any running ApolloService so it can
        // wrap around with an immediate update.
        Intent updateIntent = new Intent(ApolloService.SERVICECMD);
        updateIntent.putExtra(ApolloService.CMDNAME, AppWidget42Inverted.CMDAPPWIDGETUPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    /**
     * Initialize given widgets to default state, where we launch Music on
     * default click and hide actions if service not running.
     */
    private void defaultAppWidget(Context context, int[] appWidgetIds) {
        final RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.fourbytwo_app_widget_inverted);

        linkButtons(context, views, false /* not playing */);
        pushUpdate(context, appWidgetIds, views);
    }

    private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {
        // Update specific list of appWidgetIds if given, otherwise default to
        // all
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            gm.updateAppWidget(appWidgetIds, views);
        } else {
            gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }
    }

    /**
     * Check against {@link AppWidgetManager} if there are any instances of this
     * widget.
     */
    private boolean hasInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this
                .getClass()));
        return (appWidgetIds.length > 0);
    }

    /**
     * Handle a change notification coming over from {@link ApolloService}
     */
    public void notifyChange(ApolloService service, String what) {
        if (hasInstances(service)) {
            if (ApolloService.META_CHANGED.equals(what)
                    || ApolloService.PLAYSTATE_CHANGED.equals(what)
                    || ApolloService.REPEATMODE_CHANGED.equals(what)
                    || ApolloService.SHUFFLEMODE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }

    /**
     * Update all active widget instances by pushing changes
     */
    public void performUpdate(ApolloService service, int[] appWidgetIds) {
        final RemoteViews views = new RemoteViews(service.getPackageName(),
                R.layout.fourbytwo_app_widget_inverted);

        CharSequence artistName = service.getArtistName();
        CharSequence albumName = service.getAlbumName();
        CharSequence trackName = service.getTrackName();
        views.setTextViewText(R.id.four_by_two_artistname_inverted, artistName);
        views.setTextViewText(R.id.four_by_two_albumname_inverted, albumName);
        views.setTextViewText(R.id.four_by_two_trackname_inverted, trackName);

        // Set album art
        AQuery aq = new AQuery(service);
        Bitmap bitmap = aq.getCachedImage(ApolloUtils.getImageURL(service.getAlbumName(),
                ALBUM_IMAGE, service));
        if (bitmap != null) {
            views.setViewVisibility(R.id.four_by_two_albumart_inverted, View.VISIBLE);
            views.setImageViewBitmap(R.id.four_by_two_albumart_inverted, bitmap);
        } else {
            views.setViewVisibility(R.id.four_by_two_albumart_inverted, View.GONE);
        }

        // Set correct drawable for pause state
        final boolean playing = service.isPlaying();
        if (playing) {
            views.setImageViewResource(R.id.four_by_two_control_play_inverted,
                    R.drawable.apollo_holo_dark_pause);
        } else {
            views.setImageViewResource(R.id.four_by_two_control_play_inverted,
                    R.drawable.apollo_holo_dark_play);
        }

        // Set correct drawable for repeat state
        switch (service.getRepeatMode()) {
            case ApolloService.REPEAT_ALL:
                views.setImageViewResource(R.id.four_by_two_control_repeat_inverted,
                        R.drawable.apollo_holo_light_repeat_all);
                break;
            case ApolloService.REPEAT_CURRENT:
                views.setImageViewResource(R.id.four_by_two_control_repeat_inverted,
                        R.drawable.apollo_holo_light_repeat_one);
                break;
            default:
                views.setImageViewResource(R.id.four_by_two_control_repeat_inverted,
                        R.drawable.apollo_holo_dark_repeat_normal);
                break;
        }

        // Set correct drawable for shuffle state
        switch (service.getShuffleMode()) {
            case ApolloService.SHUFFLE_NONE:
                views.setImageViewResource(R.id.four_by_two_control_shuffle_inverted,
                        R.drawable.apollo_holo_dark_shuffle_normal);
                break;
            case ApolloService.SHUFFLE_AUTO:
                views.setImageViewResource(R.id.four_by_two_control_shuffle_inverted,
                        R.drawable.apollo_holo_light_shuffle_on);
                break;
            default:
                views.setImageViewResource(R.id.four_by_two_control_shuffle_inverted,
                        R.drawable.apollo_holo_light_shuffle_on);
                break;
        }
        // Link actions buttons to intents
        linkButtons(service, views, playing);

        pushUpdate(service, appWidgetIds, views);

    }

    /**
     * Link up various button actions using {@link PendingIntents}.
     * 
     * @param playerActive True if player is active in background, which means
     *            widget click will launch {@link MediaPlaybackActivity},
     *            otherwise we launch {@link MusicBrowserActivity}.
     */
    private void linkButtons(Context context, RemoteViews views, boolean playerActive) {

        // Connect up various buttons and touch events
        Intent intent;
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(context, ApolloService.class);

        if (playerActive) {
            intent = new Intent(context, AudioPlayerHolder.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.four_by_two_albumart_inverted, pendingIntent);
            views.setOnClickPendingIntent(R.id.four_by_two_info_inverted, pendingIntent);
        } else {
            intent = new Intent(context, MusicLibrary.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.four_by_two_albumart_inverted, pendingIntent);
            views.setOnClickPendingIntent(R.id.four_by_two_info_inverted, pendingIntent);
        }

        intent = new Intent(ApolloService.TOGGLEPAUSE_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_control_play_inverted, pendingIntent);

        intent = new Intent(ApolloService.NEXT_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_control_next_inverted, pendingIntent);

        intent = new Intent(ApolloService.PREVIOUS_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_control_prev_inverted, pendingIntent);

        intent = new Intent(ApolloService.CYCLEREPEAT_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_control_repeat_inverted, pendingIntent);

        intent = new Intent(ApolloService.TOGGLESHUFFLE_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.four_by_two_control_shuffle_inverted, pendingIntent);
    }
}
