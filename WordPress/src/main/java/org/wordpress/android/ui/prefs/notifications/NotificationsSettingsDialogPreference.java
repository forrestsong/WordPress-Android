package org.wordpress.android.ui.prefs.notifications;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.wordpress.android.R;
import org.wordpress.android.models.NotificationsSettings;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.JSONUtils;
import org.wordpress.android.util.StringUtils;

import javax.annotation.Nonnull;

// A dialog preference that displays settings for a NotificationSettings Channel and Type
public class NotificationsSettingsDialogPreference extends DialogPreference {
    private NotificationsSettings.Channel mChannel;
    private NotificationsSettings.Type mType;
    private JSONObject mSettingsJson;
    private JSONObject mUpdatedJson = new JSONObject();

    private long mSiteId;

    private OnSiteSettingsChangedListener mOnNotificationsSettingsChangedListener;

    public interface OnSiteSettingsChangedListener {
        void OnNotificationsSettingsChanged(NotificationsSettings.Channel channel, NotificationsSettings.Type type, long siteId, JSONObject newValues);
    }

    public NotificationsSettingsDialogPreference(Context context, AttributeSet attrs, NotificationsSettings.Channel channel,
                                                 NotificationsSettings.Type type, long siteId, JSONObject settings,
                                                 OnSiteSettingsChangedListener listener) {
        super(context, attrs);

        mChannel = channel;
        mType = type;
        mSiteId = siteId;
        mSettingsJson = settings;
        mOnNotificationsSettingsChangedListener = listener;
    }

    @Override
    protected void onBindDialogView(@Nonnull View view) {
        super.onBindDialogView(view);
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout view = new LinearLayout(getContext());
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        view.setOrientation(LinearLayout.VERTICAL);


        return configureLayoutForView(view);
    }

    private View configureLayoutForView(LinearLayout view) {
        String[] settingsArray, settingsValues;
        if (mChannel == NotificationsSettings.Channel.SITES) {
            settingsArray = getContext().getResources().getStringArray(R.array.notifications_site_settings);
            settingsValues = getContext().getResources().getStringArray(R.array.notifications_site_settings_values);
        } else {
            settingsArray = getContext().getResources().getStringArray(R.array.notifications_other_settings);
            settingsValues = getContext().getResources().getStringArray(R.array.notifications_other_settings_values);
        }

        if (settingsArray != null && settingsArray.length == settingsValues.length) {
            for (int i=0; i < settingsArray.length; i++) {

                String settingName = settingsArray[i];
                String settingValue = settingsValues[i];
                View commentsSetting = View.inflate(getContext(), R.layout.notifications_settings_switch, null);
                TextView title = (TextView)commentsSetting.findViewById(R.id.notifications_switch_title);
                title.setText(settingName);

                Switch toggleSwitch = (Switch)commentsSetting.findViewById(R.id.notifications_switch);
                toggleSwitch.setChecked(JSONUtils.queryJSON(mSettingsJson, settingValue, true));
                toggleSwitch.setTag(settingValue);
                toggleSwitch.setOnCheckedChangeListener(mOnCheckedChangedListener);

                view.addView(commentsSetting);
            }
        }

        return view;
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            try {
                String settingName = StringUtils.notNullStr(compoundButton.getTag().toString());
                if (mUpdatedJson.has(settingName)) {
                    mUpdatedJson.remove(settingName);
                } else {
                    mUpdatedJson.put(compoundButton.getTag().toString(), isChecked);
                }

                mSettingsJson.put(compoundButton.getTag().toString(), isChecked);
            } catch (JSONException e) {
                AppLog.e(AppLog.T.NOTIFS, "Could not add notification setting change to JSONObject");
            }
        }
    };

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mUpdatedJson.length() > 0 && mOnNotificationsSettingsChangedListener != null) {
            mOnNotificationsSettingsChangedListener.OnNotificationsSettingsChanged(mChannel, mType, mSiteId, mUpdatedJson);
        }
    }
}