package com.feisttech.trendsevidence;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Picture;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class MainActivity extends Activity {
    private static final int PICK_CSV = 101, SAVE_ZIP = 102, PICK_PLAN = 103, PICK_PNG = 104, SAVE_LEDGER = 105;
    private WebView web;
    private LinearLayout loadingOverlay;
    private EditText address, label, notes;
    private TextView status, heading, queueStatus;
    private LinearLayout root, toolbar, queueBar, controls, panel, actions;
    private HorizontalScrollView queueScroller, controlScroller;
    private ScrollView bottom;
    private final ArrayList<Button> buttons = new ArrayList<>();
    private JSONArray ledger = new JSONArray();
    private boolean paused = false, forensic = false;
    private byte[] csvBytes, pendingZip, importedPng;
    private ArrayList<String> queue = new ArrayList<>();
    private CasePlan importedPlan;
    private int queueIndex = 0;
    private boolean throttled = false;
    private String csvName;
    private boolean navigating = false;
    private boolean desktopMode = true;
    private CaptureStore captureStore;
    private final Handler runner = new Handler(Looper.getMainLooper());
    private boolean autoRun = false;
    private long delayMs = 300000;
    private String pendingCaptureId;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        WebView.enableSlowWholeDocumentDraw();
        captureStore=new CaptureStore(this);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); setContentView(root);
        heading = new TextView(this);heading.setTextSize(17);heading.setPadding(12,6,12,4);root.addView(heading);
        Button menu=button("☰  Menu / Hide",root);menu.setOnClickListener(v->toggleMenus());
        toolbar = new LinearLayout(this); toolbar.setOrientation(LinearLayout.HORIZONTAL);
        address = new EditText(this); address.setSingleLine(true); address.setText("https://trends.google.com/trends/explore");
        toolbar.addView(address,new LinearLayout.LayoutParams(0,-2,1));
        Button go = button("Open", toolbar); go.setOnClickListener(v -> open(address.getText().toString())); root.addView(toolbar);
        queueBar = new LinearLayout(this);queueScroller=new HorizontalScrollView(this);queueScroller.addView(queueBar);root.addView(queueScroller);
        Button importPlan=button("Import plan",queueBar);importPlan.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_PLAN);});
        Button cases=button("Cases",queueBar);cases.setOnClickListener(v->showCases());
        Button queries=button("Queries",queueBar);queries.setOnClickListener(v->showJobs());
        Button next=button("Next",queueBar);next.setOnClickListener(v->advance());
        Button browser=button("Browser",queueBar);browser.setOnClickListener(v->openExternal());
        Button collected=button("Collected",queueBar);collected.setOnClickListener(v->showCollected());
        controls=new LinearLayout(this);controlScroller=new HorizontalScrollView(this);controlScroller.addView(controls);root.addView(controlScroller);
        Button skin=button("Switch skin",controls);skin.setOnClickListener(v->{forensic=!forensic;getPreferences(MODE_PRIVATE).edit().putBoolean("forensic",forensic).apply();applySkin();});
        Button pause=button("Pause / Resume",controls);pause.setOnClickListener(v->{paused=!paused;getPreferences(MODE_PRIVATE).edit().putBoolean("paused",paused).apply();record(paused?"PAUSED":"RESUMED", "user action");updateQueueStatus();});
        Button ledgerButton=button("Export ledger",controls);ledgerButton.setOnClickListener(v->exportLedger());
        Button languages=button("Languages",controls);languages.setOnClickListener(v->showLanguageBuilder());
        Button run=button("Run",controls);run.setOnClickListener(v->runOptions());
        Button settings=button("Settings",controls);settings.setOnClickListener(v->showSettings());
        queueStatus=new TextView(this);queueStatus.setPadding(12,3,12,5);root.addView(queueStatus);
        web = new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true);
        desktopMode=getPreferences(MODE_PRIVATE).getBoolean("desktopMode",true);setViewingMode(desktopMode);
        web.getSettings().setBuiltInZoomControls(true); web.getSettings().setDisplayZoomControls(false);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient(){
            @Override public void onReceivedHttpError(WebView view,android.webkit.WebResourceRequest request,android.webkit.WebResourceResponse response){
                if(request.isForMainFrame()&&response.getStatusCode()==429){throttled=true;paused=true;loadingOverlay.setVisibility(View.GONE);getPreferences(MODE_PRIVATE).edit().putBoolean("paused",true).apply();record("HTTP_429",request.getUrl().toString());view.stopLoading();status.setText("Google returned HTTP 429. Queue paused; resume later.");updateQueueStatus();}
            }
            @Override public void onPageFinished(WebView view,String url){navigating=false;loadingOverlay.setVisibility(View.GONE);address.setText(url);if(!throttled)status.setText("Loaded: " + url);if(autoRun&&!throttled)runner.postDelayed(()->{if(autoRun&&url.equals(web.getUrl()))capture();},10000);}
            @Override public void onReceivedError(WebView view,android.webkit.WebResourceRequest request,android.webkit.WebResourceError error){if(request.isForMainFrame()){navigating=false;loadingOverlay.setVisibility(View.GONE);if(autoRun){autoRun=false;record("LOAD_FAILED",error.getDescription().toString());}status.setText("Page load failed: "+error.getDescription());}}
            @Override public boolean shouldOverrideUrlLoading(WebView view,String url){return !url.startsWith("https://trends.google.com/");}
        });
        FrameLayout webFrame=new FrameLayout(this);webFrame.addView(web,new FrameLayout.LayoutParams(-1,-1));
        loadingOverlay=new LinearLayout(this);loadingOverlay.setOrientation(LinearLayout.VERTICAL);loadingOverlay.setGravity(android.view.Gravity.CENTER);loadingOverlay.setBackgroundColor(Color.rgb(247,250,255));
        loadingOverlay.addView(new LoadingGif(this),new LinearLayout.LayoutParams(dp(220),dp(220)));
        TextView loadingText=new TextView(this);loadingText.setText("Loading search insights…");loadingText.setTextColor(Color.rgb(30,47,70));loadingText.setTextSize(16);loadingText.setGravity(android.view.Gravity.CENTER);loadingOverlay.addView(loadingText);
        loadingOverlay.setVisibility(View.GONE);webFrame.addView(loadingOverlay,new FrameLayout.LayoutParams(-1,-1));root.addView(webFrame,new LinearLayout.LayoutParams(-1,0,1));
        bottom = new ScrollView(this); panel = new LinearLayout(this);panel.setPadding(12,4,12,8);panel.setOrientation(LinearLayout.VERTICAL);bottom.addView(panel);
        label = new EditText(this); label.setHint("Capture label: Run A / phone A");panel.addView(label);
        notes = new EditText(this); notes.setHint("Observation notes");panel.addView(notes);
        actions = new LinearLayout(this);panel.addView(actions);
        Button csv=button("Attach CSV",actions);csv.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_CSV);});
        Button png=button("Attach PNG",actions);png.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/png");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_PNG);});
        Button save=button("Capture page",actions);save.setOnClickListener(v->capture());
        Button skip=button("Mark skipped",panel);skip.setOnClickListener(v->{record("SKIPPED",notes.getText().toString());advance();});
        status=new TextView(this);status.setText("Open a Trends query, then capture its visible chart.");panel.addView(status);
        root.addView(bottom,new LinearLayout.LayoutParams(-1,-2));
        String stored=getPreferences(MODE_PRIVATE).getString("queue","");
        try(FileInputStream in=openFileInput("case-plan.csv")){ByteArrayOutputStream saved=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)saved.write(buffer,0,n);importedPlan=CasePlan.parse(saved.toString("UTF-8"));}catch(Exception ignored){}
        if(importedPlan==null)try(InputStream in=getAssets().open("core-case-plan.csv")){ByteArrayOutputStream bundled=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)bundled.write(buffer,0,n);importedPlan=CasePlan.parse(bundled.toString("UTF-8"));}catch(Exception ignored){}
        if(!stored.isEmpty()){for(String u:stored.split("\n"))if(!u.isEmpty())queue.add(u);queueIndex=Math.min(getPreferences(MODE_PRIVATE).getInt("index",0),Math.max(0,queue.size()-1));if(!queue.isEmpty())address.setText(queue.get(queueIndex));}
        forensic=getPreferences(MODE_PRIVATE).getBoolean("forensic",false);paused=getPreferences(MODE_PRIVATE).getBoolean("paused",false);delayMs=getPreferences(MODE_PRIVATE).getLong("delayMs",300000);
        try{ledger=new JSONArray(getPreferences(MODE_PRIVATE).getString("ledger","[]"));}catch(Exception ignored){ledger=new JSONArray();}
        applySkin();updateQueueStatus();toggleMenus();
        if(state!=null)web.restoreState(state); else status.setText(queue.isEmpty()?(importedPlan==null?"Import a query plan, or open a Trends URL.":"Core cases ready. Tap Menu, then Cases to choose one."):"Queue loaded. Open Browser for the selected query.");
        if(!getPreferences(MODE_PRIVATE).getBoolean("welcomeSeen",false)){
            ImageView art=new ImageView(this);art.setImageResource(getResources().getIdentifier("trendpulse_splash","drawable",getPackageName()));art.setScaleType(ImageView.ScaleType.FIT_CENTER);
            new android.app.AlertDialog.Builder(this).setTitle("Welcome to TrendPulse").setView(art).setPositiveButton("Start exploring",(dialog,which)->getPreferences(MODE_PRIVATE).edit().putBoolean("welcomeSeen",true).apply()).show();
        }
    }
    private Button button(String text,LinearLayout parent){Button b=new Button(this);b.setText(text);b.setTextSize(11);parent.addView(b);buttons.add(b);return b;}
    private void setViewingMode(boolean desktop){
        if(web==null)return;web.getSettings().setUseWideViewPort(desktop);web.getSettings().setLoadWithOverviewMode(desktop);
        String ua=web.getSettings().getUserAgentString();if(desktop)web.getSettings().setUserAgentString(ua.replace("; wv","").replace("Mobile ",""));else web.getSettings().setUserAgentString(null);
    }
    private void showSettings(){String[] options={"Desktop view (reload page)","Mobile view (reload page)","About & methodology"};new AlertDialog.Builder(this).setTitle("Settings").setItems(options,(d,n)->{
        if(n<2){desktopMode=n==0;getPreferences(MODE_PRIVATE).edit().putBoolean("desktopMode",desktopMode).apply();setViewingMode(desktopMode);if(web.getUrl()!=null)web.reload();record("VIEW_MODE",desktopMode?"desktop":"mobile");}
        else showAbout();
    }).show();}
    private void showAbout(){new AlertDialog.Builder(this).setTitle(forensic?"Signature Investigator · Methodology":"TrendPulse · Reading results").setMessage("TrendPulse 1.0 beta. A Trends score is a sampled, normalized index for the selected phrases, time, place, category and search type. The peak within a comparison is 100; it is not a search count. Changing a companion phrase changes the scale. Quoted phrases preserve the intended search syntax. Solo and grouped results are separate scales. A spike is an observed chart pattern; compare captures with the full query group, filters, timestamp and original CSV. The raw screenshot and stamped copy are both saved in Collected with hashes. A zero, a failed load and a 429 have different meanings.").setPositiveButton("Close",null).show();}
    private void toggleMenus(){int visibility=toolbar.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE;toolbar.setVisibility(visibility);queueScroller.setVisibility(visibility);controlScroller.setVisibility(visibility);bottom.setVisibility(visibility);}
    private void runOptions(){
        String[] choices={"15 seconds","1 minute","5 minutes","15 minutes","30 minutes"};long[] values={15000,60000,300000,900000,1800000};
        if(autoRun){autoRun=false;runner.removeCallbacksAndMessages(null);record("RUN_STOPPED","user action");status.setText("Automatic run stopped; your position is saved.");return;}
        new AlertDialog.Builder(this).setTitle("Delay between searches").setSingleChoiceItems(choices,2,(dialog,index)->{delayMs=values[index];getPreferences(MODE_PRIVATE).edit().putLong("delayMs",delayMs).apply();dialog.dismiss();new AlertDialog.Builder(this).setTitle("Start from selected query?").setMessage("The app will load, wait 10 seconds, capture the page, then wait "+choices[index]+" before the next query. Keep the app open. Pause or tap Run to stop.").setNegativeButton("Cancel",null).setPositiveButton("Start",(d,w)->{if(queue.isEmpty()){status.setText("Select a case or build a language query first.");return;}paused=false;autoRun=true;record("RUN_STARTED","delayMs="+delayMs);open(queue.get(queueIndex));}).show();}).setNegativeButton("Cancel",null).show();
    }
    private void showCollected(){
        try{JSONArray captures=captureStore.list();String[] labels=new String[captures.length()];for(int i=0;i<captures.length();i++){JSONObject c=captures.getJSONObject(i);JSONObject source=c.optJSONObject("source");labels[i]=c.optString("capturedAt")+"  ·  "+(source==null?"":source.optString("q"))+"  ·  "+(source==null?"":source.optString("geo"));}
            if(labels.length==0){status.setText("No captures yet. Open a query and tap Capture page.");return;}
            new AlertDialog.Builder(this).setTitle("Collected captures ("+labels.length+")").setItems(labels,(d,index)->captureOptions(captures.optJSONObject(index).optString("id"))).setPositiveButton("Close",null).show();
        }catch(Exception e){status.setText("Collection error: "+e.getMessage());}
    }
    private void captureOptions(String id){
        try{JSONObject m=new JSONObject(new String(CaptureStore.read(captureStore.file(id,"manifest.json")),"UTF-8"));String[] actions={"View record","Export ZIP","Delete capture"};new AlertDialog.Builder(this).setTitle(m.optString("capturedAt")).setItems(actions,(dialog,which)->{
            try{if(which==0)new AlertDialog.Builder(this).setTitle("Capture record").setMessage(m.toString(2)).setPositiveButton("Close",null).show();
                if(which==1){pendingZip=captureStore.zip(id);pendingCaptureId=id;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/zip");intent.putExtra(Intent.EXTRA_TITLE,"trendpulse-"+id+".zip");startActivityForResult(intent,SAVE_ZIP);}
                if(which==2)new AlertDialog.Builder(this).setTitle("Delete this capture?").setNegativeButton("Keep",null).setPositiveButton("Delete",(d,w)->{captureStore.delete(id);record("CAPTURE_DELETED","id="+id);showCollected();}).show();
            }catch(Exception e){status.setText("Capture action failed: "+e.getMessage());}
        }).show();}catch(Exception e){status.setText("Cannot read capture: "+e.getMessage());}
    }
    private void showCases(){
        if(importedPlan==null||importedPlan.cases.isEmpty()){status.setText("Import a case plan CSV to browse its cases.");return;}
        final ArrayList<String> ids=new ArrayList<>(importedPlan.cases.keySet());String[] choices=new String[ids.size()];
        for(int i=0;i<ids.size();i++){String id=ids.get(i);choices[i]=importedPlan.labels.get(id)+"  ·  "+importedPlan.cases.get(id).size()+" queries";}
        new AlertDialog.Builder(this).setTitle("Select a case").setItems(choices,(dialog,which)->selectCase(ids.get(which))).setNegativeButton("Cancel",null).show();
    }
    private void selectCase(String id){
        queue.clear();for(CasePlan.Row row:importedPlan.cases.get(id))queue.add(row.url);
        queueIndex=0;StringBuilder stored=new StringBuilder();for(String u:queue)stored.append(u).append('\n');
        getPreferences(MODE_PRIVATE).edit().putString("queue",stored.toString()).putString("selectedCase",id).putInt("index",0).apply();
        if(!queue.isEmpty())address.setText(queue.get(0));record("CASE_SELECTED","caseId="+id+"; jobs="+queue.size());
        status.setText(importedPlan.labels.get(id)+": "+queue.size()+" queries. Select a query or open the first one.");updateQueueStatus();showJobs();
    }
    private void showJobs(){
        if(queue.isEmpty()){status.setText("Choose a case or make a language plan first.");return;}
        String id=getPreferences(MODE_PRIVATE).getString("selectedCase","");java.util.List<CasePlan.Row> rows=importedPlan==null?null:importedPlan.cases.get(id);
        String[] choices=new String[queue.size()];for(int i=0;i<queue.size();i++){
            if(rows!=null&&i<rows.size()&&queue.get(i).equals(rows.get(i).url))choices[i]=(i+1)+" · "+rows.get(i).termsJson+" · "+rows.get(i).geo+" · cat "+rows.get(i).category;
            else choices[i]=(i+1)+" · "+Uri.parse(queue.get(i)).getQueryParameter("q");
        }
        new AlertDialog.Builder(this).setTitle("Choose query · "+queue.size()+" available").setSingleChoiceItems(choices,queueIndex,(dialog,index)->{queueIndex=index;getPreferences(MODE_PRIVATE).edit().putInt("index",index).apply();address.setText(queue.get(index));record("JOB_SELECTED","index="+index);dialog.dismiss();status.setText("Selected query "+(index+1)+". Tap Open or Browser.");}).setNegativeButton("Close",null).show();
    }
    private void showLanguageBuilder(){
        ScrollView scroll=new ScrollView(this);LinearLayout fields=new LinearLayout(this);fields.setOrientation(LinearLayout.VERTICAL);fields.setPadding(20,8,20,8);scroll.addView(fields);
        String[] labels={"English (anchor)","עברית · Hebrew","Español · Spanish","Français · French","العربية · Arabic","中文 · Chinese"};
        EditText[] input=new EditText[6];
        for(int i=0;i<6;i++){
            TextView name=new TextView(this);name.setText(labels[i]);fields.addView(name);
            input[i]=new EditText(this);input[i].setSingleLine(true);input[i].setHint(i==0?"Exact source phrase":"Enter or paste an approved translation");
            input[i].setInputType(InputType.TYPE_CLASS_TEXT);if(i==1||i==4)input[i].setTextDirection(View.TEXT_DIRECTION_RTL);fields.addView(input[i]);
        }
        new AlertDialog.Builder(this).setTitle("Compare one concept across languages")
            .setMessage("Enter exact strings. Review translations and names before running. Five terms fit in one chart; a sixth makes a second chart with English as anchor.")
            .setView(scroll).setNegativeButton("Cancel",null).setPositiveButton("Build queries",(d,w)->{
                try{
                    ArrayList<LanguagePlanner.Variant> variants=new ArrayList<>();
                    for(int i=0;i<6;i++){String term=input[i].getText().toString().trim();if(!term.isEmpty())variants.add(new LanguagePlanner.Variant(LanguagePlanner.LANGUAGES[i],term,"user-entered",true));}
                    if(variants.isEmpty()||!"en".equals(variants.get(0).language))throw new IllegalArgumentException("Enter the English anchor first");
                    Uri current=Uri.parse(address.getText().toString());
                    String dates=current.getQueryParameter("date"),geo=current.getQueryParameter("geo"),property=current.getQueryParameter("gprop"),category=current.getQueryParameter("cat");
                    String[] range=dates==null?new String[]{"2025-01-01","2025-12-31"}:dates.split(" ");
                    if(range.length!=2)throw new IllegalArgumentException("Choose a start/end date in the current query first");
                    int cat=category==null?0:Integer.parseInt(category);
                    java.util.List<LanguagePlanner.Query> planned=LanguagePlanner.cascade(variants,range[0],range[1],geo==null?"US":geo,property==null?"":property,cat);
                    StringBuilder list=new StringBuilder();queue.clear();for(LanguagePlanner.Query q:planned){queue.add(q.url);list.append(q.url).append('\n');}
                    queueIndex=0;csvBytes=null;importedPng=null;getPreferences(MODE_PRIVATE).edit().putString("queue",list.toString()).putInt("index",0).apply();
                    address.setText(queue.get(0));record("LANGUAGE_CASCADE_CREATED","groups="+queue.size()+"; query contexts saved in URL order");
                    status.setText("Created "+queue.size()+" language group(s). Inspect each URL; different groups have separate 0–100 scales.");
                }catch(Exception error){status.setText("Language plan: "+error.getMessage());}
            }).show();
    }
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    private static final class LoadingGif extends View {
        private Movie movie;private long started;
        LoadingGif(Activity activity){super(activity);try(InputStream in=activity.getAssets().open("loading.gif")){movie=Movie.decodeStream(in);}catch(Exception ignored){}}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(movie==null)return;if(started==0)started=android.os.SystemClock.uptimeMillis();int duration=movie.duration();if(duration<1)duration=1000;movie.setTime((int)((android.os.SystemClock.uptimeMillis()-started)%duration));float scale=Math.min(getWidth()/(float)Math.max(1,movie.width()),getHeight()/(float)Math.max(1,movie.height()));canvas.save();canvas.translate((getWidth()-movie.width()*scale)/2f,(getHeight()-movie.height()*scale)/2f);canvas.scale(scale,scale);movie.draw(canvas,0,0);canvas.restore();if(getVisibility()==VISIBLE)postInvalidateDelayed(40);}
    }
    private void applySkin(){int bg=forensic?Color.rgb(13,19,28):Color.rgb(247,250,255),fg=forensic?Color.rgb(224,244,230):Color.rgb(30,47,70),accent=forensic?Color.rgb(54,209,114):Color.rgb(38,105,221);root.setBackgroundColor(bg);for(LinearLayout l:new LinearLayout[]{toolbar,queueBar,panel,actions})l.setBackgroundColor(bg);for(TextView t:new TextView[]{heading,queueStatus,status,label,notes,address})t.setTextColor(fg);for(Button b:buttons){b.setTextColor(accent);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(forensic?Color.rgb(30,47,49):Color.rgb(229,239,255)));}heading.setText(forensic?"SIGNATURE INVESTIGATOR  ·  EVIDENCE RUNNER":"TrendPulse  ·  Search Insights");updateQueueStatus();}
    private void updateQueueStatus(){if(queueStatus!=null)queueStatus.setText((paused?"PAUSED  ·  ":"READY  ·  ")+(queue.isEmpty()?"Import a CSV query plan":("Job "+(queueIndex+1)+" of "+queue.size()))+"  ·  "+ledger.length()+" ledger events");}
    private void record(String event,String detail){try{JSONObject row=new JSONObject().put("timeUtc",now()).put("event",event).put("jobIndex",queue.isEmpty()?JSONObject.NULL:queueIndex).put("url",queue.isEmpty()?address.getText().toString():queue.get(queueIndex)).put("detail",detail);ledger.put(row);getPreferences(MODE_PRIVATE).edit().putString("ledger",ledger.toString()).apply();updateQueueStatus();}catch(Exception e){status.setText("Ledger error: "+e.getMessage());}}
    private void exportLedger(){try{JSONObject document=new JSONObject().put("format","TrendPulseRunLedger/1").put("exportedAtUtc",now()).put("jobCount",queue.size()).put("currentJobIndex",queueIndex).put("events",ledger);byte[] payload=(document.toString(2)+"\n").getBytes("UTF-8");pendingZip=payload;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"trendpulse-run-ledger.json");startActivityForResult(i,SAVE_LEDGER);}catch(Exception e){status.setText("Ledger export failed: "+e.getMessage());}}
    private void advance(){if(queue.isEmpty()){status.setText("Import a query plan first.");return;}if(paused){status.setText("Queue paused. Tap Pause / Resume to continue.");return;}if(queueIndex+1>=queue.size()){status.setText("End of queue: "+queue.size()+" queries.");return;}queueIndex++;getPreferences(MODE_PRIVATE).edit().putInt("index",queueIndex).apply();address.setText(queue.get(queueIndex));csvBytes=null;csvName=null;importedPng=null;throttled=false;record("JOB_SELECTED", "advanced");status.setText("Query "+(queueIndex+1)+" / "+queue.size()+" ready. Open Browser to inspect.");}
    private void openExternal(){if(paused){status.setText("Queue paused. Tap Pause / Resume to continue.");return;}try{Uri u=Uri.parse(address.getText().toString());if(!"https".equals(u.getScheme())||!"trends.google.com".equals(u.getHost()))throw new IllegalArgumentException();startActivity(new Intent(Intent.ACTION_VIEW,u));record("BROWSER_OPENED","external browser; outcome pending");status.setText("Browser opened query "+(queueIndex+1)+" / "+queue.size()+". Return to attach PNG/CSV.");}catch(Exception e){status.setText("Invalid Trends URL or browser unavailable.");}}
    private void open(String value){if(paused){status.setText("Queue paused. Tap Pause / Resume to continue.");return;}try{Uri uri=Uri.parse(value.trim());if(!"https".equals(uri.getScheme())||!"trends.google.com".equals(uri.getHost()))throw new IllegalArgumentException();throttled=false;navigating=true;loadingOverlay.setVisibility(View.VISIBLE);record("WEBVIEW_OPENED","loading");web.loadUrl(uri.toString());}catch(Exception e){status.setText("Enter a https://trends.google.com/ URL.");}}
    private void capture(){
        if(importedPng!=null){String target=address.getText().toString();if(!target.startsWith("https://trends.google.com/trends/explore")){status.setText("Select a Trends Explore URL first.");return;}try{packageCaptureBytes(importedPng,target,"user-selected-png");}catch(Exception e){status.setText(e.getMessage());}return;}
        if(throttled){status.setText("HTTP 429 paused in-app capture. Attach a screenshot from the browser, or resume later.");return;}
        if(navigating||web.getProgress()<100){status.setText("Wait for the page to finish loading.");return;}
        String url=web.getUrl();if(url==null||!url.startsWith("https://trends.google.com/trends/explore")){status.setText("Open a Google Trends Explore query first.");return;}
        try{
            Picture picture=web.capturePicture();int w=picture.getWidth(),h=picture.getHeight();
            if(w<1||h<1)throw new IllegalStateException("Page has no drawable content");
            double factor=Math.min(1.0,Math.sqrt(24000000.0/((double)w*h)));
            int outW=Math.max(1,(int)(w*factor)),outH=Math.max(1,(int)(h*factor));
            Bitmap bitmap=Bitmap.createBitmap(outW,outH,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);canvas.scale(outW/(float)w,outH/(float)h);picture.draw(canvas);
            packageCapture(bitmap,url);
        }catch(Exception e){status.setText("Full-page capture failed: "+e.getMessage());if(autoRun){autoRun=false;record("CAPTURE_FAILED",e.toString());}}
    }
    private String now(){SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.format(new Date());}
    private String hash(byte[] bytes)throws Exception{byte[] d=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder s=new StringBuilder();for(byte b:d)s.append(String.format(Locale.US,"%02x",b&255));return s.toString();}
    private void entry(ZipOutputStream z,String name,byte[] bytes)throws Exception{z.putNextEntry(new ZipEntry(name));z.write(bytes);z.closeEntry();}
    private void packageCapture(Bitmap bitmap,String url){
        try{ByteArrayOutputStream shot=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.PNG,100,shot);bitmap.recycle();packageCaptureBytes(shot.toByteArray(),url,"in-app-full-document");}catch(Exception e){status.setText("Capture packaging failed: "+e.getMessage());}
    }
    private byte[] stamp(byte[] raw,String url,String date)throws Exception{
        Bitmap original=BitmapFactory.decodeByteArray(raw,0,raw.length);if(original==null)throw new IllegalArgumentException("Image cannot be decoded");
        Bitmap annotated=original.copy(Bitmap.Config.ARGB_8888,true);original.recycle();Canvas c=new Canvas(annotated);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);float text=Math.max(16,Math.min(30,annotated.getWidth()/45f));p.setTextSize(text);
        int padding=(int)(text*.6f),lines=Math.max(1,(int)Math.ceil(url.length()/(double)Math.max(18,(int)(annotated.getWidth()/(text*.55f)))));
        int banner=(int)((lines+2)*text*1.35f);p.setColor(0xDD102237);c.drawRect(0,0,annotated.getWidth(),banner,p);p.setColor(Color.WHITE);
        c.drawText(date, padding,text+padding,p);int chunk=Math.max(18,(int)(annotated.getWidth()/(text*.55f)));
        for(int i=0;i<url.length();i+=chunk)c.drawText(url.substring(i,Math.min(url.length(),i+chunk)),padding,(float)((i/chunk+2)*text*1.3+padding),p);
        ByteArrayOutputStream out=new ByteArrayOutputStream();annotated.compress(Bitmap.CompressFormat.PNG,100,out);annotated.recycle();return out.toByteArray();
    }
    private void packageCaptureBytes(byte[] png,String url,String method){
        try{
            Uri uri=Uri.parse(url);String timestamp=now();JSONObject source=new JSONObject();
            source.put("captureMethod",method).put("queueIndex",queue.isEmpty()?JSONObject.NULL:queueIndex).put("queueLength",queue.size()).put("url",url).put("rawQuery",uri.getEncodedQuery()).put("q",uri.getQueryParameter("q")).put("date",uri.getQueryParameter("date")).put("geo",uri.getQueryParameter("geo")).put("gprop",uri.getQueryParameter("gprop")).put("cat",uri.getQueryParameter("cat")).put("title",method.equals("user-selected-png")?JSONObject.NULL:web.getTitle()).put("userAgent",method.equals("user-selected-png")?JSONObject.NULL:web.getSettings().getUserAgentString()).put("viewportWidth",method.equals("user-selected-png")?JSONObject.NULL:web.getWidth()).put("viewportHeight",method.equals("user-selected-png")?JSONObject.NULL:web.getHeight());
            byte[] stamped=stamp(png,url,timestamp);JSONObject files=new JSONObject();files.put("screenshot.png",new JSONObject().put("sha256",hash(png)).put("bytes",png.length));files.put("stamped.png",new JSONObject().put("sha256",hash(stamped)).put("bytes",stamped.length));
            if(csvBytes!=null)files.put("data.csv",new JSONObject().put("sha256",hash(csvBytes)).put("bytes",csvBytes.length).put("originalFilename",csvName));
            JSONObject manifest=new JSONObject().put("format","TrendPulseCapture/1").put("capturedAt",timestamp).put("source",source).put("observer",new JSONObject().put("label",label.getText().toString()).put("notes",notes.getText().toString()).put("appVersion","1.0.0")).put("files",files);
            String id=captureStore.save(png,stamped,csvBytes,manifest);record("CAPTURE_SAVED","id="+id+"; method="+method+"; screenshotSha256="+hash(png)+(csvBytes==null?"":"; csvSha256="+hash(csvBytes)));
            status.setText("Saved to Collected: "+id.substring(0,8));csvBytes=null;importedPng=null;
            if(autoRun){if(queueIndex+1<queue.size())runner.postDelayed(()->{if(autoRun&&!paused){advance();open(queue.get(queueIndex));}},delayMs);else{autoRun=false;record("RUN_COMPLETE","jobs="+queue.size());status.setText("Run complete. Open Collected to review captures.");}}
        }catch(Exception e){status.setText("Capture packaging failed: "+e.getMessage());}
    }
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null)return;
        try{if(request==PICK_CSV){Uri uri=data.getData();String name="selected.csv";android.database.Cursor c=getContentResolver().query(uri,new String[]{android.provider.OpenableColumns.DISPLAY_NAME},null,null,null);if(c!=null){if(c.moveToFirst())name=c.getString(0);c.close();}ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(uri)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>10_000_000)throw new IllegalArgumentException("CSV exceeds 10 MB");}}csvBytes=out.toByteArray();csvName=name;status.setText("Attached "+csvName+" ("+csvBytes.length+" bytes).");}
            if(request==PICK_PNG){ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(data.getData())){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>15_000_000)throw new IllegalArgumentException("PNG exceeds 15 MB");}}importedPng=out.toByteArray();status.setText("Screenshot attached ("+importedPng.length+" bytes). Ready to export with the queued URL.");}
            if(request==PICK_PLAN){ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(data.getData())){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>5_000_000)throw new IllegalArgumentException("Plan exceeds 5 MB");}}CasePlan parsed=CasePlan.parse(out.toString("UTF-8"));if(parsed.count==0)throw new IllegalArgumentException("No queries found");try(FileOutputStream saved=openFileOutput("case-plan.csv",MODE_PRIVATE)){saved.write(out.toByteArray());}importedPlan=parsed;record("PLAN_IMPORTED","cases="+parsed.cases.size()+"; jobs="+parsed.count+"; planSha256="+hash(out.toByteArray()));status.setText("Imported "+parsed.cases.size()+" cases. Tap Cases to choose one.");showCases();}
            if((request==SAVE_ZIP||request==SAVE_LEDGER)&&pendingZip!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(pendingZip);}status.setText(request==SAVE_ZIP?"ZIP saved. Preserve it for comparison.":"Run ledger saved.");pendingZip=null;}
        }catch(Exception e){status.setText("File operation failed: "+e.getMessage());}}
    @Override protected void onSaveInstanceState(Bundle out){web.saveState(out);super.onSaveInstanceState(out);}
    @Override protected void onDestroy(){autoRun=false;runner.removeCallbacksAndMessages(null);if(web!=null)web.destroy();super.onDestroy();}
    @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}
