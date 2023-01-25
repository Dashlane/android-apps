package com.dashlane.securearchive

import android.os.Bundle
import androidx.activity.viewModels
import com.dashlane.securearchive.databinding.ActivityBackupBinding
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackupActivity : DashlaneActivity() {

    private val viewModel: BackupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BackupViewProxy(this, binding, viewModel)
    }
}