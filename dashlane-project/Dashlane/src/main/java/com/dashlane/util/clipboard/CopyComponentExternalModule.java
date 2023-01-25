package com.dashlane.util.clipboard;

import com.dashlane.followupnotification.services.FollowUpNotificationVaultItemCopyListenerImpl;
import com.dashlane.util.clipboard.vault.VaultItemCopyListener;
import com.dashlane.util.clipboard.vault.VaultItemCopyListenerHolder;
import com.dashlane.util.clipboard.vault.VaultItemCopyListenerHolderByInjection;
import com.dashlane.util.clipboard.vault.VaultItemVisibleCopyEdgeCases;
import com.dashlane.util.clipboard.vault.VaultItemVisibleCopyEdgeCasesImpl;
import com.dashlane.vault.VaultItemLogCopyListener;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;



@Module
public class CopyComponentExternalModule {

    @Provides
    public VaultItemCopyListenerHolder providesVaultItemCopyListenerHolder(
            FollowUpNotificationVaultItemCopyListenerImpl followUpNotificationVaultItemCopyListener,
            VaultItemLogCopyListener vaultItemLogCopyListener
    ) {
        List<VaultItemCopyListener> list = new ArrayList<>();
        list.add(followUpNotificationVaultItemCopyListener);
        list.add(vaultItemLogCopyListener);

        return new VaultItemCopyListenerHolderByInjection(list);
    }

    @Provides
    public ClipboardCopy providesClipboardCopy(ClipboardCopyImpl impl) {
        return impl;
    }

    @Provides
    public VaultItemVisibleCopyEdgeCases providesVaultItemVisibleCopyEdgeCases(VaultItemVisibleCopyEdgeCasesImpl impl) {
        return impl;
    }
}