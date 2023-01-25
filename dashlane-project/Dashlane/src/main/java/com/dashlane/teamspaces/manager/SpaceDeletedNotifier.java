package com.dashlane.teamspaces.manager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.dashlane.network.BaseNetworkResponse;
import com.dashlane.network.webservices.SpaceDeletedService;
import com.dashlane.preference.UserPreferencesManager;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class SpaceDeletedNotifier {
    private static final String PREF_SPACE_IDS = "notifyItemsDeletedSpaceIds";

    private static final Set<String> SPACE_IDS_SEND_IN_PROGRESS = new HashSet<>();
    private final UserPreferencesManager mPreferencesManager;
    private final SpaceDeletedService mSpaceDeletedService;

    @Inject
    public SpaceDeletedNotifier(UserPreferencesManager preferencesManager, SpaceDeletedService service) {
        mPreferencesManager = preferencesManager;
        mSpaceDeletedService = service;
    }

    @VisibleForTesting
    static void clearSpaceIdsInProgress() {
        SPACE_IDS_SEND_IN_PROGRESS.clear();
    }

    public void sendIfNeeded(String username, String uki) {
        Set<String> spaceIds = mPreferencesManager.getStringSet(PREF_SPACE_IDS);
        if (spaceIds == null || spaceIds.isEmpty()) {
            return;
        }
        for (String spaceId : spaceIds) {
            notifyDeleted(spaceId, username, uki);
        }
    }

    public void storeSpaceToDelete(String spaceId) {
        Set<String> spaceIds = mPreferencesManager.getStringSet(PREF_SPACE_IDS);
        if (spaceIds == null) {
            spaceIds = new HashSet<>();
        } else if (spaceIds.contains(spaceId)) {
            return; 
        }
        spaceIds.add(spaceId);
        mPreferencesManager.putStringSet(PREF_SPACE_IDS, spaceIds);
    }

    @VisibleForTesting
    void notifyDeleted(String spaceId, String login, String uki) {
        if (SPACE_IDS_SEND_IN_PROGRESS.contains(spaceId)) {
            return;
        }
        SPACE_IDS_SEND_IN_PROGRESS.add(spaceId);
        callDeleteSpace(spaceId, login, uki);
    }

    @VisibleForTesting
    void callDeleteSpace(final String spaceId, String login, String uki) {
        mSpaceDeletedService.createCall(login, uki, spaceId).enqueue(new Callback<BaseNetworkResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<BaseNetworkResponse<String>> call,
                                   @NonNull Response<BaseNetworkResponse<String>> response) {
                SPACE_IDS_SEND_IN_PROGRESS.remove(spaceId);
                if (response.isSuccessful()) {
                    onSpaceDeleted(spaceId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseNetworkResponse<String>> call, @NonNull Throwable t) {
                SPACE_IDS_SEND_IN_PROGRESS.remove(spaceId);
            }
        });
    }

    @VisibleForTesting
    void onSpaceDeleted(String spaceId) {
        Set<String> spaceIds = mPreferencesManager.getStringSet(PREF_SPACE_IDS);
        if (spaceIds == null || !spaceIds.contains(spaceId)) {
            return; 
        }
        spaceIds.remove(spaceId);
        mPreferencesManager.putStringSet(PREF_SPACE_IDS, spaceIds);
    }
}
