package com.example.util

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.model.LauncherApp
import com.example.ui.LauncherViewModel

object AppLauncherHelper {
    fun launchAppSafely(activity: FragmentActivity?, app: LauncherApp, viewModel: LauncherViewModel) {
        if (app.isLocked && activity != null) {
            BiometricHelper.authenticate(
                activity = activity,
                onSuccess = { viewModel.launchApp(app) },
                onError = { Toast.makeText(activity, "Falha na autenticação: $it", Toast.LENGTH_SHORT).show() }
            )
        } else {
            viewModel.launchApp(app)
        }
    }
}
