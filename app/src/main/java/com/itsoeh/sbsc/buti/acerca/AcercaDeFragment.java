package com.itsoeh.sbsc.buti.acerca;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.itsoeh.sbsc.buti.R;

/**
 * Pantalla "Acerca de": qué es B'ÜTI, objetivo, créditos y versión.
 */
public class AcercaDeFragment extends Fragment {

    public AcercaDeFragment() {
        super(R.layout.fragment_acerca_de);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        TextView txtVersion = view.findViewById(R.id.txtVersionAcercaDe);

        // Toma la versión del build.gradle (versionName) para no escribirla a mano.
        try {
            PackageInfo info = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);

            txtVersion.setText("Versión " + info.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            txtVersion.setText("Versión 1.0.0");
        }
    }
}
