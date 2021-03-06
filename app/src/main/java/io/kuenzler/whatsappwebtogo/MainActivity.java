package io.kuenzler.whatsappwebtogo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //private static final String js = "function wrapFunc(name) {" +
    //        "if(typeof window[name] == 'function') {" +
    //        "var original = window['__' + name] = window[name];" +
    //       "window[name] = function() {  // and replace with wrapper" +
    //       "var result = original.apply(this, arguments);" +
    //       "Interceptor.reportCall(name, result.toString());" +
    //      "return result;" +
    //       "} alert('hallo'); } else {alert('hallo')}" +
    //      "}";

    // private static final String js = "function wrapFunc(name) {alert('works!); if (typeof window[name] == 'function') {var original = window['__' + name] = window[name]; window[name] = function() { var result = original.apply(this, arguments); Interceptor.reportCall(name, result.toString()); return result; } alert('yes'); } else { alert('no'); }}";
    // private static final String audioJs = "javascript:window.onload=function(){var n=document.getElementsByTagName(\"audio\"),r=n.length;for(var o=0;o<r;o++)n[o].setAttribute(\"index\",o),n[o].addEventListener(\"ended\",function(){for(var e=0;e<r;e++)this===n[e]&&window.external.setEndedIndex(e)})}";
    
    private static final String androidCurrent = "Linux; U; Android " + Build.VERSION.RELEASE;
    private static final String chrome = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    /*
    private static final String osxYosemity = "Macintosh; Intel Mac OS X 10_10_1";
    private static final String windows10 = "Windows 10";
    private static final String windows95 = "Windows 95";
    private static final String windows30 = "Windows 3.0";
    private static final String android8 = "Linux; U; Android 8.0.0; ko-kr; LG-L160L Build/IML74K";
    private static final String toGo = "Linux; U; Android WhatsappWebToGo";
    private static final String safari = "AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.1.3 Safari/7046A194A";
    private static final String firefox = "Gecko/20100101 Firefox/40.1";
    private static final String edge = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063";
    */

    private static final String browser = chrome;
    private static final String device = androidCurrent;
    private static final String userAgent = "Mozilla/5.0 (" + device + ") " + browser;

    private static final String CAMERA_PERMISSION = "android.permission.CAMERA";
    private static final String AUDIO_PERMISSION = "android.permission.RECORD_AUDIO";
    private static final String[] VIDEO_PERMISSION = {CAMERA_PERMISSION, AUDIO_PERMISSION};

    private static final String WHATSAPP_WEB_URL = "https://web.whatsapp.com";

    private static final String WEBVIEW_PACKAGE = "com.google.android.webview";
    private static final String[] NOUGAT_WEBVIEW_PACKAGES = {WEBVIEW_PACKAGE, "com.android.chrome", "com.chrome.beta", "com.chrome.canary", "com.chrome.dev"};

    private static final int FILECHOOSER_RESULTCODE = 200;
    private static final int CAMERA_PERMISSION_RESULTCODE = 201;
    private static final int AUDIO_PERMISSION_RESULTCODE = 202;
    private static final int VIDEO_PERMISSION_RESULTCODE = 203;

    private static final String DEBUG_TAG = "WAWEBTOGO";

    private WebView webView;
    private ViewGroup mainView;

    private long lastTouchClick = 0;
    private long lastBackClick = 0;
    private float lastXClick = 0;
    private float lastYClick = 0;

    private final Activity activity = this;

    private ValueCallback<Uri[]> mUploadMessage;
    private PermissionRequest currentPermissionRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mainView = findViewById(R.id.layout);
        webView = findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true); //for wa web

        webView.getSettings().setAllowContentAccess(true); // for camera
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.getSettings().setMediaPlaybackRequiresUserGesture(false); //for audio messages

        webView.getSettings().setDomStorageEnabled(true); //for html5 app

        webView.getSettings().setAppCacheEnabled(true); // app cache
        webView.getSettings().setAppCachePath(getCacheDir().getAbsolutePath()); //app cache
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); //app cache

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (request.getResources()[0].equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED
                            && ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION, AUDIO_PERMISSION}, VIDEO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION}, CAMERA_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else {
                        request.grant(request.getResources());
                    }
                } else if (request.getResources()[0].equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    }
                } else {
                    try {
                        request.grant(request.getResources());
                    } catch (RuntimeException e) {
                        Log.d(DEBUG_TAG, "Granting permissions failed", e);
                    }
                }
            }

            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(DEBUG_TAG, "WebView console message: " + cm.message());
                return super.onConsoleMessage(cm);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                Intent chooserIntent = fileChooserParams.createIntent();
                MainActivity.this.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                view.scrollTo(0, 0);
                showSnackbar("Unblock keyboard with the keyboard button on top");

                // inject wrapper
                // don't forget to remove newline chars
                // webView.loadUrl("javascript:" + js);

                // wrap all the functions needed
                //String[] funcToWrap = new String[]{"parseMsgNotification", "func2"};
                //for (String f : funcToWrap) {
                //    webView.loadUrl("javascript:wrapFunc('" + f + "');");
                //}

                //webView.loadUrl("javascript:wrapFunc('parseMsgNotification')");
                // webView.loadUrl("javascript:wrapFunc('alert')");
                //webView.loadUrl("javascript:Interceptor.test('hallo')");

                // if (Build.VERSION.SDK_INT >= 19) {
                //     view.evaluateJavascript(audioJs, (String s) -> {
                //         //ignore
                //     });
                //} else {
                //    view.loadUrl(audioJs);
                //}

                //webView.loadUrl("javascript:window.Interceptor.showHTML" +
                //        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //whatsapp sound:
                // url.equals("https://web.whatsapp.com/assets/0a598282e94e87dea63e466d115e4a83.mp3"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if (request.getUrl().toString().contains("web.whatsapp.com")) {
                        return false;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                        startActivity(intent);
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if (url.contains("web.whatsapp.com")) {
                        return false;
                    } else {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                String msg = String.format("Error: %s - %s", error.getErrorCode(), error.getDescription());
                Log.d(DEBUG_TAG, msg);
            }

            public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                Log.d(DEBUG_TAG, "Unhandled key event: " + event.toString());
            }
        });

        // webView.addJavascriptInterface(new NotificationInterface(this), "Android");
        // webView.addJavascriptInterface(new FunctionCallInterceptor(), "Interceptor");

        webView.getSettings().setUserAgentString(userAgent);
        if (savedInstanceState == null) {
            webView.loadUrl(WHATSAPP_WEB_URL);
        } else {
            Log.d(DEBUG_TAG, "savedInstanceState is present");
        }

        webView.setOnTouchListener((View v, MotionEvent event) -> {
            if(event.getAction() == MotionEvent.ACTION_MOVE){
                return false;
            }
            if (mainView.getDescendantFocusability() == ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    && event.getAction() == MotionEvent.ACTION_UP
                    && Math.abs(event.getY() - webView.getHeight()) < 160) {
                if (System.currentTimeMillis() - lastTouchClick < 1300) {
                    if (Math.abs(lastXClick - event.getX()) < 180) {
                        showSnackbar("Use the keyboard button on top to type");
                        lastTouchClick = 0;
                    } else {
                        lastTouchClick = System.currentTimeMillis();
                        lastXClick = event.getX();
                        lastYClick = event.getY();
                    }
                } else {
                    lastTouchClick = System.currentTimeMillis();
                    lastXClick = event.getX();
                    lastYClick = event.getY();
                }
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        checkWebviewInstallation();
        mainView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        showSnackbar("Unblock keyboard with the keyboard button on top");
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_keyboard:
                toggleKeyboard();
                break;
            case R.id.scroll_left:
                showToast("scroll left");
                runOnUiThread(() -> webView.scrollTo(0, 0));
                break;
            case R.id.scroll_right:
                showToast("scroll right");
                runOnUiThread(() -> webView.scrollTo(2000, 0));
                break;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case VIDEO_PERMISSION_RESULTCODE:
                if (permissions.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        currentPermissionRequest.grant(currentPermissionRequest.getResources());
                    } catch (RuntimeException e) {
                        Log.e(DEBUG_TAG, "Granting permissions failed", e);
                    }
                } else {
                    showSnackbar("Permission not granted, can't use video.");
                    currentPermissionRequest.deny();
                }
                break;
            case CAMERA_PERMISSION_RESULTCODE:
            case AUDIO_PERMISSION_RESULTCODE:
                //same same
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        currentPermissionRequest.grant(currentPermissionRequest.getResources());
                    } catch (RuntimeException e) {
                        Log.e(DEBUG_TAG, "Granting permissions failed", e);
                    }
                } else {
                    showSnackbar("Permission not granted, can't use " + (requestCode == CAMERA_PERMISSION_RESULTCODE ? "camera" : "microphone"));
                    currentPermissionRequest.deny();
                }
                break;
            default:
                Log.d(DEBUG_TAG, "Got permission result with unknown request code " + requestCode + " - " + Arrays.asList(permissions).toString());
        }
        currentPermissionRequest = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILECHOOSER_RESULTCODE:
                if (resultCode == RESULT_CANCELED || data.getData() == null) {
                    mUploadMessage.onReceiveValue(null);
                } else {
                    Uri result = data.getData();
                    Uri[] results = new Uri[1];
                    results[0] = result;
                    mUploadMessage.onReceiveValue(results);
                }
                break;
            default:
                Log.d(DEBUG_TAG, "Got activity result with unknown request code " + requestCode + " - " + data.toString());
        }
    }

    private void checkWebviewInstallation() {
        boolean promtForWebview = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //oreo has static getter for webview package
            PackageInfo webViewPackageInfo = WebView.getCurrentWebViewPackage();
            promtForWebview = (webViewPackageInfo == null);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Nougat can choose between webview and chrome
            boolean webviewFound = false;
            for (String webviewPackage : NOUGAT_WEBVIEW_PACKAGES) {
                try {
                    getPackageManager().getPackageInfo(webviewPackage, 0);
                    webviewFound = true;
                } catch (PackageManager.NameNotFoundException e) {
                    //ignore
                }
            }
            promtForWebview = !webviewFound;
        } else {
            // all below nougat: only webview counts
            try {
                getPackageManager().getPackageInfo(WEBVIEW_PACKAGE, 0);
            } catch (PackageManager.NameNotFoundException | IllegalArgumentException e) {
                promtForWebview = true;
            }
        }

        if (!promtForWebview) {
            return;
        }

        DialogInterface.OnClickListener dialogClickListener = (DialogInterface dialog, int which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + WEBVIEW_PACKAGE)));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + WEBVIEW_PACKAGE)));
                    }
                    showToast("Please install WebView and restart Whatsapp Web To Go");
                    activity.finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("For this app to work properly, Android WebView has to be installed and up to date. " +
                "Would you like to install/update it now?\nIf you select no, you may experience problems.")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("WhatsappWeb To Go\n\nby Leonhard Künzler\n" +
                "android@kuenzler.io\n\ngithub.com/92lleo/WhatsappWebToGo\n\n" +
                "(c)2017\n\nv0.8.0")
                .setCancelable(false)
                .setPositiveButton("Ok", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showToast(String msg) {
        this.runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }

    private void showSnackbar(String msg) {
        this.runOnUiThread(() -> {
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT);
            snackbar.setAction("dismiss", (View view) -> snackbar.dismiss());
            snackbar.show();
        });
    }

    private void toggleKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (mainView.getDescendantFocusability() == ViewGroup.FOCUS_BLOCK_DESCENDANTS) {
            mainView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            showSnackbar("Unblocking keyboard...");
            //inputMethodManager.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
        } else {
            mainView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            webView.getRootView().requestFocus();
            showSnackbar("Blocking keyboard...");
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        //close drawer if open and impl. press back again to leave
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (System.currentTimeMillis() - lastBackClick < 1100) {
            finishAffinity();
        } else {
            showToast("Click back again to close");
            lastBackClick = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_hide) {
            MenuItem view = findViewById(R.id.nav_hide);
            if (getSupportActionBar().isShowing()) {
                showSnackbar("hiding... swipe right to show navigation bar");
                getSupportActionBar().hide();
            } else {
                getSupportActionBar().show();
            }
        } else if (id == R.id.nav_logout) {
            showSnackbar("logging out...");
            webView.loadUrl("javascript:localStorage.clear()");
            WebStorage.getInstance().deleteAllData();
            webView.reload();
        } else if (id == R.id.nav_new) {
            //showToast("nav_new");
        } else if (id == R.id.nav_switch) {
            //showToast("nav_switch");
        } else if (id == R.id.nav_settings) {
            //showToast("nav_settings");

            // NavigationView navigationView= findViewById(R.id.nav_settings)
            // Menu menuNav=navigationView.getMenu();
            // MenuItem nav_item2 = menuNav.findItem(R.id.nav_item2);
            // R.id.nav_settings.setEnabled(false)
            // walk(getApplicationContext().getFilesDir());
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //  walk(new File(getApplicationContext().getDataDir().toString() + "/app_webview/"));
            // }
        } else if (id == R.id.nav_about) {
            showAbout();
        } else if (id == R.id.nav_reload) {
            showSnackbar("reloading...");
            webView.reload();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // JS stuff
    /*
    public class NotificationInterface {
        Context mContext;

        NotificationInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
        }
    }

    class FunctionCallInterceptor {
        @JavascriptInterface
        public void reportCall(String functionName, String result) {
            showToast(functionName);
        }

        @JavascriptInterface
        public void showHtml(String toShow) {
            Log.d("html", toShow);
            showToast(toShow);
        }

        @JavascriptInterface
        public void setEndedIndex(int pIndex) {
            showToast("setEndedIndex: " + pIndex);
        }
    }
    */
}
