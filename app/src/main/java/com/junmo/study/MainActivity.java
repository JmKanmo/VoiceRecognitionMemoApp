package com.junmo.study;

import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    DBHelper dbHelper;
    SQLiteDatabase db;

    final Context context = this;
    String stt_file = "stt_file.db";
    String tts_file = "tts_file.db";
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferences1;

    ArrayList<String> arrayList;

    private TextToSpeech mTTS;
    SpeechRecognizer speechRecognizer;

    AudioManager am;
    int volume;

    int pitch_val = 50, speed_val = 50;
    SeekBar pitch_bar, speed_bar;

    String[] perMissionList = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
    };

    Intent i;
    private boolean clickFlag = false;
    private boolean micFlag = false;
    private boolean cntFlag = false;

    ImageView drawer_Btn;
    ImageView tts_Btn;
    ImageView stt_Btn;
    ImageView menu_Btn;
    ImageView stt_Reset;
    ImageView tts_Reset;
    ImageView tts_EditBtn;
    ScrollView tts_ScrollView;
    LinearLayout tts_LinearLayout;
    LinearLayout.LayoutParams params;
    TextView tts_TextView;
    TextView stt_TextView;

    int n;
    String text;

    RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsDB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            switch (error) {
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    stopListen();
                    startListen();
                    break;

                case SpeechRecognizer.ERROR_NETWORK:
                    stopListen();
                    startListen();
                    break;

                case SpeechRecognizer.ERROR_AUDIO:
                    stopListen();
                    startListen();
                    break;

                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    try {
                        Thread.sleep(70);
                        cntFlag = true;
                        stt_Btn.performClick();
                        stt_Btn.performClick();
                        cntFlag = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;

                case SpeechRecognizer.ERROR_CLIENT:
                    stopListen();
                    startListen();
                    break;

                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    stopListen();
                    startListen();
                    break;

                case SpeechRecognizer.ERROR_NO_MATCH:
                    stopListen();
                    startListen();
                    break;

                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    stopListen();
                    startListen();
                    break;

                case SpeechRecognizer.ERROR_SERVER:
                    stopListen();
                    startListen();
                    break;
            }
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResults = results.getStringArrayList(key);
            String[] rs = new String[mResults.size()];
            mResults.toArray(rs);
            stt_TextView.append(rs[0] + "\n\n");
            startListen();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    private void startListen() {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(i);
    }

    private void stopListen() {
        speechRecognizer.cancel();
        speechRecognizer.stopListening();
        speechRecognizer.destroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(
                this,  // 현재 화면의 제어권자
                stt_file,// db 이름
                null,  // 커서팩토리-null : 표준커서가 사용됨
                1);       // 버전
        try {
            db = dbHelper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {
            e.printStackTrace();
            finish(); // 액티비티 종료
        }

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final View drawerView = (View) findViewById(R.id.drawer);

        final LinearLayout sign_language_layout = findViewById(R.id.sign_language_layout);
        final LinearLayout mode_layout = findViewById(R.id.mode_layout);
        final LinearLayout exit_layout = findViewById(R.id.exit_layout);

        drawer_Btn = findViewById(R.id.drawer_btn);
        menu_Btn = findViewById(R.id.menu_btn);
        tts_EditBtn = findViewById(R.id.tts_Edit);
        stt_Btn = findViewById(R.id.stt_Btn);
        tts_Btn = findViewById(R.id.tts_Btn);
        stt_Reset = findViewById(R.id.stt_reset);
        tts_Reset = findViewById(R.id.tts_reset);
        tts_ScrollView = findViewById(R.id.tts_ScrollView);
        tts_LinearLayout = findViewById(R.id.tts_linearlayout);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        params.topMargin = Math.round(18 * dm.density);
        stt_TextView = findViewById(R.id.stt_TextView);

        stt_TextView.setMovementMethod(new ScrollingMovementMethod());

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        for (String permission : perMissionList) {
            int check = checkCallingOrSelfPermission(permission);

            if (check == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perMissionList, 0);
            }
        }

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.KOREAN);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    } else {
                        tts_Btn.setEnabled(true);
                    }
                } else {
                }
            }
        });

        stt_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickFlag) clickFlag = false;
                else clickFlag = true;

                if (cntFlag) ;
                else {
                    if (clickFlag) {
                        stt_Btn.setImageResource(R.drawable.onbtn);
                        Toast.makeText(getApplicationContext(), "Mic ON", Toast.LENGTH_SHORT).show();
                    } else {
                        stt_Btn.setImageResource(R.drawable.offbtn);
                        Toast.makeText(getApplicationContext(), "MIC OFF", Toast.LENGTH_SHORT).show();
                    }
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
                }
                if (clickFlag) startListen();
                else stopListen();

                if (micFlag != true) micFlag = true;
                else micFlag = false;
            }
        });

        drawer_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        sign_language_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "수화로 말하기 클릭!", Toast.LENGTH_SHORT).show();
            }
        });

        mode_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "스마트폰모드 클릭!", Toast.LENGTH_SHORT).show();
            }
        });

        exit_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        drawerLayout.setDrawerListener(myDrawerListener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        menu_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder popDialog = new AlertDialog.Builder(context);
                final LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

                final View Viewlayout = inflater.inflate(R.layout.activity_dialog,
                        (ViewGroup) findViewById(R.id.layout_dialog));

                final TextView pitch_txt = (TextView) Viewlayout.findViewById(R.id.pitch_txt); // txtItem1
                final TextView speed_txt = (TextView) Viewlayout.findViewById(R.id.speed_txt); // txtItem2

                popDialog.setIcon(android.R.drawable.btn_star_big_on);
                popDialog.setTitle("확성기 조절하기");
                popDialog.setView(Viewlayout);

                //  pitch_bar

                pitch_bar = (SeekBar) Viewlayout.findViewById(R.id.pitch_bar);
                pitch_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //Do something here with new value
                        pitch_txt.setText("음 높낮이 : " + progress);
                        pitch_val = progress;
                    }

                    public void onStartTrackingTouch(SeekBar arg0) {
                        // TODO Auto-generated method stub
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }
                });

                //  speed_bar

                speed_bar = (SeekBar) Viewlayout.findViewById(R.id.speed_bar);
                speed_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //Do something here with new value
                        speed_txt.setText("빠르기 : " + progress);
                        speed_val = progress;
                    }

                    public void onStartTrackingTouch(SeekBar arg0) {
                        // TODO Auto-generated method stub
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }
                });

                pitch_bar.setProgress(pitch_val);
                speed_bar.setProgress(speed_val);
                // Button OK

                popDialog.setPositiveButton("완료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                popDialog.create();
                popDialog.show();
            }
        });

        tts_EditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(context);
                ad.setTitle("Write and Speak :)"); // 제목 설정
                ad.setIcon(R.drawable.smile_emotion);

                final EditText ttsEdit = new EditText(context);
                ttsEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                // EditText 삽입하기
                ad.setView(ttsEdit);

                ttsEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            InputMethodManager inputMgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        }
                    }
                });

                // 취소 버튼 설정
                ad.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });

                // 말하기 버튼 설정
                ad.setNeutralButton("말하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String msg = ttsEdit.getText().toString();

                        if (msg == null || msg.isEmpty() == true || spaceCheck(msg)) {
                        } else {
                            text = msg;
                            if (micFlag == true) stt_Btn.performClick();
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, 10, AudioManager.FLAG_PLAY_SOUND);
                            arrayList.add(text);
                            tts_TextView = new TextView(context);
                            tts_TextView.setText(text);
                            tts_TextView.setBackgroundResource(R.drawable.chatbubble);
                            tts_TextView.setId(++n);
                            tts_TextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
                            tts_TextView.setLayoutParams(params);
                            tts_LinearLayout.addView(tts_TextView);
                            tts_TextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    tts_ScrollView.scrollTo(0, tts_ScrollView.getChildAt(0).getBottom());
                                }
                            });
                            chatBubbleEvent(tts_TextView);
                            speak(); //관호
                        }
                        //ttsEdit.setText(null);
                    }
                });

                // 입력 버튼 설정
                ad.setNegativeButton("입력", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String msg = ttsEdit.getText().toString();

                        if (msg == null || msg.isEmpty() == true || spaceCheck(msg)) {
                        } else {
                            text = msg;
                            arrayList.add(text);
                            tts_TextView = new TextView(context);
                            tts_TextView.setText(text);
                            tts_TextView.setBackgroundResource(R.drawable.chatbubble);
                            tts_TextView.setId(++n);
                            tts_TextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
                            tts_TextView.setLayoutParams(params);
                            tts_LinearLayout.addView(tts_TextView);
                            ttsEdit.setText(null);

                            tts_TextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    tts_ScrollView.scrollTo(0, tts_ScrollView.getChildAt(0).getBottom());
                                }
                            });
                            chatBubbleEvent(tts_TextView);
                            Toast.makeText(getApplicationContext(), "입력되었습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // 창 띄우기
                ad.show();
            }
        });

        tts_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        });

        stt_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("텍스트 초기화");
                builder1.setMessage("텍스트를 초기화하시겠습니까?");

                builder1.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stt_TextView.setText("");
                    }
                });

                builder1.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder1.show();
            }
        });

        tts_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                builder2.setTitle("텍스트 초기화");
                builder2.setMessage("텍스트를 초기화하시겠습니까?");

                builder2.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tts_LinearLayout.removeAllViews();
                        arrayList.clear();
                        text = null;
                    }
                });

                builder2.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder2.show();
            }
        });
    }

    DrawerLayout.DrawerListener myDrawerListener = new DrawerLayout.DrawerListener() {

        public void onDrawerClosed(View drawerView) {
        }

        public void onDrawerOpened(View drawerView) {
        }

        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        public void onDrawerStateChanged(int newState) {
            String state;
            switch (newState) {
                case DrawerLayout.STATE_IDLE:
                    state = "STATE_IDLE";
                    break;
                case DrawerLayout.STATE_DRAGGING:
                    state = "STATE_DRAGGING";
                    break;
                case DrawerLayout.STATE_SETTLING:
                    state = "STATE_SETTLING";
                    break;
                default:
                    state = "unknown!";
            }
        }
    };

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
            }
        }
    }

    private boolean spaceCheck(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    private void speak() {
        if (micFlag == true) stt_Btn.performClick();
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 13, AudioManager.FLAG_PLAY_SOUND);

        float pitch = (pitch_bar == null) ? pitch_val / 50 : (float) pitch_bar.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (speed_bar == null) ? speed_val / 50 : (float) speed_bar.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);

        if (arrayList.size() > 0)
            mTTS.speak(arrayList.get(arrayList.size() - 1), TextToSpeech.QUEUE_FLUSH, null);
    }

    private void speak(String message) {
        if (micFlag == true) stt_Btn.performClick();
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 13, AudioManager.FLAG_PLAY_SOUND);

        float pitch = (pitch_bar == null) ? pitch_val / 50 : (float) pitch_bar.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (speed_bar == null) ? speed_val / 50 : (float) speed_bar.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void chatBubbleEvent(final TextView textView) {
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(context);
                final String message = textView.getText().toString();
                ad.setIcon(android.R.drawable.presence_audio_online);
                ad.setTitle("Dialog");
                ad.setMessage(message);

                // 말하기 버튼 설정
                ad.setNeutralButton("말하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        speak(message);
                    }
                });

                ad.setNegativeButton("복사", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setText(textView.getText());
                        Toast.makeText(getApplicationContext(), "복사되었습니다", Toast.LENGTH_SHORT).show();
                    }
                });

                // 삭제 버튼 설정
                ad.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int idx = arrayList.indexOf(textView.getText());
                        arrayList.set(idx, "");
                        textView.setText("");
                    }
                });
                // 창 띄우기
                ad.show();
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        sharedPreferences = getSharedPreferences(stt_file, Activity.MODE_PRIVATE);
        sharedPreferences1 = getSharedPreferences(tts_file, Activity.MODE_PRIVATE);
        stt_TextView.setText(sharedPreferences.getString("keyword", ""));
        text = sharedPreferences1.getString("text", "");

        arrayList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select* from mytable;", null);
        while (cursor.moveToNext()) {
            String message = cursor.getString(1);
            arrayList.add(message);
        }
        for (int i = 0; i < arrayList.size(); i++) {
            tts_TextView = new TextView(context);
            tts_TextView.setText(arrayList.get(i));
            tts_TextView.setBackgroundResource(R.drawable.chatbubble);
            tts_TextView.setId(++n);
            tts_TextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
            tts_TextView.setLayoutParams(params);
            tts_LinearLayout.addView(tts_TextView);
            tts_TextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    tts_ScrollView.scrollTo(0, tts_ScrollView.getChildAt(0).getBottom());
                }
            });
            chatBubbleEvent(tts_TextView);
        }
        db.execSQL("delete from mytable;");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        String keyword = stt_TextView.getText().toString();
        editor.putString("keyword", keyword);
        editor1.putString("text", text);
        editor.commit();
        editor1.commit();

        for (int i = 0; i < arrayList.size(); i++) {
            db.execSQL("insert into mytable (message) values('" + arrayList.get(i) + "');");
        }
        arrayList.clear();
        tts_LinearLayout.removeAllViews();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        if (clickFlag) {
            stopListen();
            stt_Btn.performClick();
        }
        super.onDestroy();
    }
}


