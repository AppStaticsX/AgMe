package com.appstaticsx.app.agmeapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TipsFragment extends Fragment {

    WebView webView;

    String[] texts = {"Loading...", "Please wait..."};

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        // Initialize WebView and ProgressBar
        webView = view.findViewById(R.id.webView);

        ProcessingCustomDialog processingcustomDialog = new ProcessingCustomDialog(requireContext());
        processingcustomDialog.setMessage(texts);
        processingcustomDialog.setAnimation("loading_anim.json"); // Name of the animation file in res/raw

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Update the progress bar based on page load progress
                if (progress < 100) {
                    processingcustomDialog.show();
                } else {
                    processingcustomDialog.dismiss();
                }
            }
        });

        // Set WebViewClient to handle redirects within the WebView
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the URL
        webView.loadUrl("https://www.agrifarming.in/ultimate-guide-to-natural-vegetable-farming");

        return view;
    }
}
