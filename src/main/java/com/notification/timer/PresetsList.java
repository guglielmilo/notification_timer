package com.notification.timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

class PresetsList {

    private static final String TAG = "PresetCardsList";

    private final ArrayList<Preset> list = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private Context context;

    void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    int addPreset(int index, Preset preset) {
        Log.d(TAG, "addPreset: index=" + index + ", preset='" + preset.toString() + "'");
        list.add(index, preset);
        for (int p = getSize(); p > index; --p) {
            savePreset(p, loadPreset(p - 1));
        }
        savePreset(index, preset);
        return getSize();
    }

    int removePreset(int index) {
        Log.d(TAG, "removePreset: index=" + index);
        list.remove(index);
        for (int p = index; p < getSize(); ++p) {
            savePreset(p, loadPreset(p + 1));
        }
        erasePreset(getSize());
        return getSize();
    }

    int swapPreset(int fromIndex, int toIndex) {
        Log.d(TAG, "movePreset: fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", list=" + toString());
        Preset preset = loadPreset(fromIndex);
        removePreset(fromIndex);
        addPreset(toIndex, preset);
        Log.d(TAG, "movePreset: list=" + toString());
        return getSize();
    }

    int indexOf(Preset preset) {
        return list.indexOf(preset);
    }

    Preset getPreset(int index) {
        return list.get(index);
    }

    private int getSize() { return list.size(); }

    private void savePreset(int index, final Preset preset) {
        if (sharedPreferences != null) {
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putLong(String.format(Locale.US, context.getString(R.string.pref_preset_array_timer), index), preset.getTimer());
            sharedPreferencesEditor.putInt(String.format(Locale.US, context.getString(R.string.pref_preset_array_sets), index), preset.getSets());
            sharedPreferencesEditor.apply();
            Log.d(TAG, "savePreset: index=" + index + ", preset='" + preset + "'");
        }
    }

    private Preset loadPreset(int index) {
        if (sharedPreferences != null) {
            long timer = sharedPreferences.getLong(String.format(Locale.US, context.getString(R.string.pref_preset_array_timer), index), -1);
            int sets = sharedPreferences.getInt(String.format(Locale.US, context.getString(R.string.pref_preset_array_sets), index), -1);
            Preset preset = new Preset(timer, sets);
            Log.d(TAG, "loadPreset: index=" + index + ", preset='" + preset + "'");
            return preset;
        } else {
            return new Preset();
        }
    }

    private void erasePreset(int index) {
        savePreset(index, new Preset());
    }

    int initPresets() {
        Log.d(TAG, "initPresets");
        int index = 0;
        list.clear();
        while (true) {
            Preset preset = loadPreset(index);
            if (preset.isValid()) {
                list.add(index++, preset);
            } else {
                break;
            }
        }
        return getSize();
    }

    boolean isSynced() {
        Log.d(TAG, "syncPresets");
        ArrayList<Preset> otherList = new ArrayList<>();
        int index = 0;
        while (true) {
            Preset preset = loadPreset(index);
            if (preset.isValid()) {
                otherList.add(index++, preset);
            } else {
                break;
            }
        }
        return otherList.equals(list);
    }

    @NonNull
    public String toString() {
        StringBuilder string = new StringBuilder("{");
        for (int index = 0; index < getSize(); ++index) {
            string.append(list.get(index));
            if (index < getSize() - 1) {
                string.append(", ");
            }
        }
        string.append("}");
        return string.toString();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof PresetsList && this.list.equals(((PresetsList) object).list);
    }
}