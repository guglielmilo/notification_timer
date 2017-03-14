package com.simpleworkout.timer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Locale;

@SuppressLint("ParcelCreator")
public class InteractiveNotification extends Notification {

    private final static String TAG = "InteractiveNotification";

    private final static int ID = 52;

    public int getId() {
        return ID;
    }

    private boolean restTimerNotificationVisible;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    public Notification getNotification() {
        return notificationBuilder.build();
    }

    // Timer service related
    private long timerCurrent, timerUser;
    private int setsCurrent, setsUser;
    private String timerString, setDoneString, setsCurrentString, setsNextString;

    private ButtonsLayout buttonsLayout;
    private ButtonAction button0, button1, button2;

    public void setVibrationEnable(boolean vibrationEnable) {
        this.vibrationEnable = vibrationEnable;
    }

    public void setVibrationReadyEnable(boolean vibrationReadyEnable) {
        this.vibrationReadyEnable = vibrationReadyEnable;
    }

    public void setLightColor(int lightColor) {
        this.lightColor = lightColor;
    }

    public void setLightReadyColor(int lightReadyColor) {
        this.lightReadyColor = lightReadyColor;
    }

    public void setRingtone(Uri ringtone) {
        this.ringtone = ringtone;
    }

    public void setRingtoneReady(Uri ringtoneReady) {
        this.ringtoneReady = ringtoneReady;
    }

    // Settings options
    private boolean vibrationEnable;
    private boolean vibrationReadyEnable;
    private int lightColor;
    private int lightReadyColor;
    private Uri ringtone;
    private Uri ringtoneReady;

    public final static int COLOR_NONE = -1;

    private Context context;

    private enum ButtonAction {

        NO_ACTION("no_action"),
        START("start"),
        STOP("stop"),
        PAUSE("pause"),
        RESUME("resume"),
        RESET("reset"),
        NEXT_SET("next_set"),
        NEXT_SET_START("next_set_start"),
        EXTRA_SET("extra_set"),
        TIMER_MINUS("timer_minus"),
        TIMER_PLUS("timer_plus"),
        DISMISS("dismiss");

        private String action;

        ButtonAction(String action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return action;
        }
    }

    protected enum ButtonsLayout {

        NO_LAYOUT("no_layout"),
        READY("ready"),
        RUNNING("running"),
        PAUSED("paused"),
        SET_DONE("set_done"),
        ALL_SETS_DONE("all_sets_done");

        private String layout;

        ButtonsLayout(String layout) {
            this.layout = layout;
        }

        @Override
        public String toString() {
            return layout;
        }
    }

    protected enum NotificationMode {

        NO_NOTIFICATION,
        UPDATE,
        LIGHT_ONLY,
        LIGHT_SOUND_SHORT_VIBRATE,
        LIGHT_SOUND_LONG_VIBRATE;
    }

    public InteractiveNotification(Context context) {

        this.context = context;

        // pending intent to go back to the main activity from the notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        // pending intent when the notification is deleted
        PendingIntent pendingIntentDeleted = PendingIntent.getBroadcast(context, 0, new Intent().setAction(IntentAction.NOTIFICATION_DISMISS), PendingIntent.FLAG_UPDATE_CURRENT);

        // Default configurations
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_1_button);
        contentView.setImageViewResource(R.id.button0Notif1, R.drawable.ic_play_arrow_black_48dp);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // prepare Notification Builder
        notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingIntentDeleted)
                .setContent(contentView)
                .setPriority(PRIORITY_MAX);

        timerString = "";
        setDoneString = "";
        setsCurrentString = "";
        setsNextString = "";
        timerCurrent = 0;
        setsCurrent = 1;
        setsUser = 1;

        button2 = ButtonAction.NO_ACTION;
        button1 = ButtonAction.NO_ACTION;
        button0 = ButtonAction.NO_ACTION;
        buttonsLayout = ButtonsLayout.NO_LAYOUT;

        updateButtonsLayout(ButtonsLayout.READY);
    }

    protected void update(int setsCurrent, long timerCurrent, ButtonsLayout layout, NotificationMode notificationMode) {
        updateButtonsLayout(layout);
        updateTimerCurrent(timerCurrent);
        updateSetsCurrent(setsCurrent, notificationMode);
    }

    protected void updateButtonsLayout(ButtonsLayout layout) {
        updateButtonsLayout(layout, NotificationMode.NO_NOTIFICATION);
    }

    protected void updateButtonsLayout(ButtonsLayout layout, NotificationMode notificationMode) {
        Log.d(TAG, "updateButtonsLayout: layout=" + layout.toString() + ", buttonsLayout=" + buttonsLayout.toString() + ", timerCurrent=" + timerCurrent + ", setsCurrent=" + setsCurrent);
        if (buttonsLayout == ButtonsLayout.RUNNING || buttonsLayout != layout) {
            switch (layout) {
                case READY:
                    button2 = ButtonAction.NO_ACTION;
                    button1 = ButtonAction.DISMISS;
                    button0 = ButtonAction.START;
                    notificationBuilder.setOngoing(false);
                    break;
                case RUNNING:
                    button2 = ButtonAction.NEXT_SET;
                    button1 = ButtonAction.TIMER_MINUS;
                    button0 = ButtonAction.PAUSE;
                    notificationBuilder.setOngoing(true);
                    break;
                case PAUSED:
                    button2 = ButtonAction.DISMISS;
                    button1 = ButtonAction.NEXT_SET;
                    button0 = ButtonAction.RESUME;
                    notificationBuilder.setOngoing(false);
                    break;
                case SET_DONE:
                    button2 = ButtonAction.NO_ACTION;
                    button1 = ButtonAction.DISMISS;
                    button0 = ButtonAction.START;
                    notificationBuilder.setOngoing(false);
                    break;
                case ALL_SETS_DONE:
                    button2 = ButtonAction.DISMISS;
                    button1 = ButtonAction.RESET;
                    button0 = ButtonAction.EXTRA_SET;
                    notificationBuilder.setOngoing(false);
                    break;
                default:
                    Log.e(TAG, "updateButtonsLayout: layout=" + layout.toString());
            }
            buttonsLayout = layout;
            Log.d(TAG, "updateButtonsLayout: buttonsLayout=" + buttonsLayout.toString());

            updateTimerTextView();
            updateSetsTextView();

            build(notificationMode);
        }
    }

    private void updateButton(RemoteViews remoteView, int id, ButtonAction action) {

        int iconId;
        Intent intent;

        switch (action) {
            case START:
                iconId = R.drawable.ic_play_arrow_black_48dp;
                intent = new Intent().setAction(IntentAction.START);
                break;
            case PAUSE:
                iconId = R.drawable.ic_pause_black_48dp;
                intent = new Intent().setAction(IntentAction.PAUSE);
                break;
            case RESUME:
                iconId = R.drawable.ic_play_arrow_black_48dp;
                intent = new Intent().setAction(IntentAction.RESUME);
                break;
            case STOP:
                iconId = R.drawable.ic_stop_black_48dp;
                intent = new Intent().setAction(IntentAction.STOP);
                break;
            case NEXT_SET:
                iconId = R.drawable.ic_chevron_right_black_48dp;
                intent = new Intent().setAction(IntentAction.NEXT_SET);
                break;
            case NEXT_SET_START:
                iconId = R.drawable.ic_chevron_right_double_black_48dp;
                intent = new Intent().setAction(IntentAction.NEXT_SET_START);
                break;
            case EXTRA_SET:
                iconId = R.drawable.ic_chevron_right_double_black_48dp;
                intent = new Intent().setAction(IntentAction.EXTRA_SET);
                break;
            case TIMER_MINUS:
                iconId = R.drawable.ic_stopwatch_remove_black_48dp;
                intent = new Intent().setAction(IntentAction.TIMER_MINUS);
                break;
            case TIMER_PLUS:
                iconId = R.drawable.ic_stopwatch_add_black_48dp;
                intent = new Intent().setAction(IntentAction.TIMER_PLUS);
                break;
            case RESET:
                iconId = R.drawable.ic_refresh_black_48dp;
                intent = new Intent().setAction(IntentAction.RESET);
                break;
            case DISMISS:
                iconId = R.drawable.ic_close_black_48dp;
                intent = new Intent().setAction(IntentAction.NOTIFICATION_DISMISS);
                break;
            default:
                Log.e(TAG, "updateButton: undefined action=" + action);
                return;
        }
        remoteView.setImageViewResource(id, iconId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(id, pendingIntent);
    }

    protected void setVisible() {
        Log.d(TAG, "setVisible");
        restTimerNotificationVisible = true;
        build(NotificationMode.UPDATE);
    }

    private RemoteViews createRemoteView() {

        RemoteViews remoteView;

        switch (buttonsLayout) {
            default:
            case NO_LAYOUT:
            case READY:
            case PAUSED:
            case RUNNING:
                if (button2 != ButtonAction.NO_ACTION) {
                    remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_3_buttons_progressbar);
                    updateButton(remoteView, R.id.button2NotifPB3, button2);
                    updateButton(remoteView, R.id.button1NotifPB3, button1);
                    updateButton(remoteView, R.id.button0NotifPB3, button0);
                    remoteView.setTextViewText(R.id.textViewTimerNotifPB3, timerString);
                    remoteView.setTextViewText(R.id.textViewSetsCurrentNotifPB3, setsCurrentString);
                    remoteView.setTextViewText(R.id.textViewSetsNextNotifPB3, setsNextString);
                    remoteView.setProgressBar(R.id.progressBarNotifPB3, (int) timerUser, (int) (timerUser - timerCurrent), false);
                } else if (button1 != ButtonAction.NO_ACTION) {
                    remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_2_buttons_progressbar);
                    updateButton(remoteView, R.id.button1NotifPB2, button1);
                    updateButton(remoteView, R.id.button0NotifPB2, button0);
                    remoteView.setTextViewText(R.id.textViewTimerNotifPB2, timerString);
                    remoteView.setTextViewText(R.id.textViewSetsCurrentNotifPB2, setsCurrentString);
                    remoteView.setTextViewText(R.id.textViewSetsNextNotifPB2, setsNextString);
                    remoteView.setProgressBar(R.id.progressBarNotifPB2, (int) timerUser, (int) (timerUser - timerCurrent), false);
                } else {
                    remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_1_button_progressbar);
                    updateButton(remoteView, R.id.button0NotifPB1, button0);
                    remoteView.setTextViewText(R.id.textViewTimerNotifPB1, timerString);
                    remoteView.setTextViewText(R.id.textViewSetsCurrentNotifPB1, setsCurrentString);
                    remoteView.setTextViewText(R.id.textViewSetsNextNotifPB1, setsNextString);
                    remoteView.setProgressBar(R.id.progressBarNotifPB1, (int) timerUser, (int) (timerUser - timerCurrent), false);
                }
                break;
            case SET_DONE:
            case ALL_SETS_DONE:
                if (button2 != ButtonAction.NO_ACTION) {
                    remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_3_buttons);
                    updateButton(remoteView, R.id.button2Notif3, button2);
                    updateButton(remoteView, R.id.button1Notif3, button1);
                    updateButton(remoteView, R.id.button0Notif3, button0);
                    remoteView.setTextViewText(R.id.textViewTimerNotif3, timerString);
                    remoteView.setTextViewText(R.id.textViewSetDoneNotif3, setDoneString);
                } else if (button1 != ButtonAction.NO_ACTION) {
                    remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_2_buttons);
                    updateButton(remoteView, R.id.button1Notif2, button1);
                    updateButton(remoteView, R.id.button0Notif2, button0);
                    remoteView.setTextViewText(R.id.textViewTimerNotif2, timerString);
                    remoteView.setTextViewText(R.id.textViewSetDoneNotif2, setDoneString);
                } else {
                    remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_1_button);
                    updateButton(remoteView, R.id.button0Notif1, button0);
                    remoteView.setTextViewText(R.id.textViewTimerNotif1, timerString);
                    remoteView.setTextViewText(R.id.textViewSetDoneNotif1, setDoneString);
                }
                break;
        }

        return remoteView;
    }

    protected void build(NotificationMode notificationMode) {
        if (restTimerNotificationVisible && notificationMode != NotificationMode.NO_NOTIFICATION) {

            RemoteViews remoteView = createRemoteView();
            notificationBuilder.setContent(remoteView);

            // pending intent to go back to the main activity from the notification
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            // pending intent when the notification is deleted
            PendingIntent pendingIntentDeleted = PendingIntent.getBroadcast(context, 0, new Intent().setAction(IntentAction.NOTIFICATION_DISMISS), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            // prepare Notification Builder
            notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setDeleteIntent(pendingIntentDeleted)
                    .setContent(remoteView)
                    .setPriority(PRIORITY_MAX);

            switch (notificationMode) {
                default:
                case UPDATE:
                    notificationBuilder.setVibrate(null);
                    notificationBuilder.setSound(null);
                    notificationBuilder.setLights(COLOR_DEFAULT, 0, 0);
                    break;
                case LIGHT_ONLY:
                    notificationBuilder.setVibrate(null);
                    notificationBuilder.setSound(null);
                    if (lightReadyColor != -1) {
                        notificationBuilder.setLights(lightReadyColor, 500, 600);
                    }
                    break;
                case LIGHT_SOUND_SHORT_VIBRATE:
                    if (vibrationReadyEnable) {
                        notificationBuilder.setVibrate(MainActivity.vibrationPattern);
                    } else {
                        notificationBuilder.setVibrate(null);
                    }
                    notificationBuilder.setSound(ringtoneReady);
                    if (lightReadyColor != COLOR_NONE) {
                        notificationBuilder.setLights(lightReadyColor, 500, 600);
                    }
                    break;
                case LIGHT_SOUND_LONG_VIBRATE:
                    if (vibrationEnable) {
                        notificationBuilder.setVibrate(MainActivity.vibrationPattern);
                    }
                    notificationBuilder.setSound(ringtone);
                    if (lightColor != COLOR_NONE) {
                        notificationBuilder.setLights(lightColor, 1000, 1000);
                    }
                    break;
            }
            notificationManager.notify(ID, notificationBuilder.build());

            if (notificationMode != NotificationMode.UPDATE) {
                Log.d(TAG, "build: notificationMode=" + notificationMode);
            }
        }
    }

    protected void updateTimerCurrent(long timer) {
        updateTimerCurrent(timer, NotificationMode.NO_NOTIFICATION);
    }

    protected void updateTimerCurrent(long timer, NotificationMode notificationMode) {
        timerCurrent = timer;
        updateTimerTextView();
        build(notificationMode);
    }

    protected void updateTimerUser(long timer) {
        timerUser = timer;
    }

    protected void updateSetsCurrent(int sets) {
        updateSetsCurrent(sets, NotificationMode.NO_NOTIFICATION);
    }

    protected void updateSetsCurrent(int sets, NotificationMode notificationMode) {
        Log.d(TAG, "updateSetsCurrent: setsCurrent=" + sets);
        setsCurrent = sets;
        updateSetsTextView();
        build(notificationMode);
    }

    protected void updateSetsUser(int sets) {
        updateSetsUser(sets, NotificationMode.NO_NOTIFICATION);
    }

    protected void updateSetsUser(int sets, NotificationMode notificationMode) {
        Log.d(TAG, "updateSetsUser: setsUser=" + sets);
        setsUser = sets;
        updateSetsTextView();
        build(notificationMode);
    }

    private void updateTimerTextView() {
        switch (buttonsLayout) {
            case NO_LAYOUT:
            case READY:
            case PAUSED:
            case RUNNING:
                timerString = String.format(Locale.US, "%d:%02d", timerCurrent / 60, timerCurrent % 60);
                break;
            case SET_DONE:
            case ALL_SETS_DONE:
                timerString = String.format(context.getString(R.string.time_is_up));
                break;
        }
    }

    private void updateSetsTextView() {
        switch (buttonsLayout) {
            case NO_LAYOUT:
            case READY:
            case PAUSED:
            case RUNNING:
                setsCurrentString = String.valueOf(setsCurrent);
                setsNextString = String.valueOf(setsCurrent + 1);
                Log.d(TAG, "updateSetsTextView: setsCurrentString='" + setsCurrentString + "', setsNextString='" + setsNextString + "'");
                break;
            case SET_DONE:
                if (setsUser == MainActivity.SETS_INFINITY) {
                    setDoneString = String.format(context.getString(R.string.current_sets_infinity), setsCurrent);
                    break;
                }
            case ALL_SETS_DONE:
                if (setsUser > 1) {
                    setDoneString = String.format(context.getString(R.string.current_sets), setsCurrent, setsUser);
                } else {
                    setDoneString = String.format(context.getString(R.string.current_set), setsCurrent, setsUser);
                }
                Log.d(TAG, "updateSetsTextView: setDoneString='" + setDoneString + "'");
                break;
        }
    }

    protected void dismiss() {
        if (restTimerNotificationVisible) {
            notificationManager.cancel(ID);
            restTimerNotificationVisible = false;
            Log.d(TAG, "dismissed");
        }
    }
}
