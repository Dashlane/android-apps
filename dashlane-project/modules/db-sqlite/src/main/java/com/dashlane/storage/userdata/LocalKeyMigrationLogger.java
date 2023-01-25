package com.dashlane.storage.userdata;

import com.dashlane.useractivity.log.install.InstallLogRepository;
import com.dashlane.useractivity.log.install.InstallLogCode28;

public class LocalKeyMigrationLogger {

    private final InstallLogRepository mInstallLogRepository;

    public LocalKeyMigrationLogger(InstallLogRepository installLogRepository) {
        mInstallLogRepository = installLogRepository;
    }

    public void logSubStep(String subStep) {
        InstallLogCode28 installLogCode28 = new InstallLogCode28(null, subStep);
        mInstallLogRepository.enqueue(installLogCode28, false);
    }

    public static final String KW_INSTALL_OPEN_DB_RAW_KEY_SUBSTEP = "94";
    public static final String KW_INSTALL_MIGRATE_DB_RAW_KEY_SUBSTEP = "95";
    public static final String KW_INSTALL_MIGRATE_DB_RAW_KEY_FAIL_PASSWORD_FALLBACK_SUBSTEP = "96";
    public static final String KW_INSTALL_MIGRATE_DB_RAW_KEY_SUCCESS_SUBSTEP = "97";
    public static final String KW_INSTALL_MIGRATE_DB_RAW_KEY_DUMP_DONE_SUBSTEP = "98";
    public static final String KW_INSTALL_MIGRATE_DB_RAW_KEY_READ_FAIL_SUBSTEP = "99";
    public static final String KW_INSTALL_MIGRATE_DB_RECOVER_CLEANUP_NEW_SUBSTEP = "100";
    public static final String KW_INSTALL_MIGRATE_DB_RECOVER_ROLLBACK_OLD_SUBSTEP = "101";
    public static final String KW_INSTALL_OPEN_DB_RAW_KEY_SUCCESS_SUBSTEP = "104";
    public static final String OPEN_DB_LEGACY_INVALID_LOCAL_KEY = "105";
}