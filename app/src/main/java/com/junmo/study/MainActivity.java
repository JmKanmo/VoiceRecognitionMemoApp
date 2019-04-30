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
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/*
    안드로이드 어플리케이션 개발
    JM_Kan_mo
 */

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener, TextToSpeech.OnInitListener {

    DBHelper dbHelper;
    SQLiteDatabase db;
    LanguageTable languageTable = new LanguageTable();
    String key_language = "", value_language = "";
    TextView text_Language;

    final Context context = this;
    String stt_file = "stt_file.db";

    SharedPreferences sharedPreferences, sharedPreferences1, sharedPreferences2, sharedPreferences3;

    ArrayList<String> arrayList, arrayList_1;

    private TextToSpeech mTTS;
    SpeechRecognizer speechRecognizer;

    AudioManager am;

    int pitch_val = 50, speed_val = 50, volume_val = 0;
    SeekBar pitch_bar, speed_bar, volume_bar;

    String[] perMissionList = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
    };

    Intent i;
    private boolean clickFlag = false, micFlag = false, speakerFlag = false, cntFlag = false;

    ImageView drawer_Btn, tts_Reset, stt_Btn, tts_Btn, menu_Btn;
    ImageView stt_Add, stt_Share, stt_Store;
    ImageView tts_EditBtn;

    ScrollView tts_ScrollView;

    LinearLayout tts_LinearLayout;
    LinearLayout.LayoutParams params;

    TextView tts_TextView, stt_TextView;

    int n, memo_idx;
    final long FINISH_INTERVAL_TIME = 2000;
    long backPressedTime;
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
                        Thread.sleep(100);
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
            stt_TextView.append(rs[0] + "\n");
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
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(i);
    }

    private void stopListen() {
        //speechRecognizer.cancel();
        //speechRecognizer.stopListening();
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
        final LinearLayout find_memo_layout = findViewById(R.id.find_memo);
        final LinearLayout select_language_layout = findViewById(R.id.select_language);
        final LinearLayout exit_layout = findViewById(R.id.exit_layout);

        drawer_Btn = findViewById(R.id.drawer_btn);
        menu_Btn = findViewById(R.id.menu_btn);
        tts_EditBtn = findViewById(R.id.tts_Edit);
        tts_Btn = findViewById(R.id.tts_Btn);
        stt_Btn = findViewById(R.id.stt_Btn);
        tts_Reset = findViewById(R.id.tts_reset);
        stt_Add = findViewById(R.id.stt_add);
        stt_Share = findViewById(R.id.stt_share);
        stt_Store = findViewById(R.id.stt_store);
        tts_ScrollView = findViewById(R.id.tts_ScrollView);
        tts_LinearLayout = findViewById(R.id.tts_linearlayout);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        params.topMargin = Math.round(18 * dm.density);
        stt_TextView = findViewById(R.id.stt_TextView);
        stt_TextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f);
        stt_TextView.setMovementMethod(new ScrollingMovementMethod());
        text_Language = findViewById(R.id.text_language);
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, value_language = "ko-KR");
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

        mTTS = new TextToSpeech(this, this);

        tts_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speakerFlag == false) {
                    final String message = stt_TextView.getText().toString();
                    speak(message);
                    tts_Btn.setImageResource(R.drawable.speaker_off);
                    speakerFlag = true;
                } else {
                    mTTS.stop();
                    speakerFlag = false;
                    tts_Btn.setImageResource(R.drawable.speaker);
                }
            }
        });

        stt_TextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (stt_TextView.getText().toString().isEmpty() != true) {
                    final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboardManager.setText(stt_TextView.getText().toString());
                    Toast.makeText(getApplicationContext(), "복사되었습니다", Toast.LENGTH_SHORT).show();
                }
                return false;
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
                        Toast.makeText(getApplicationContext(), "Mic on", Toast.LENGTH_SHORT).show();
                        if (isNetworkConnected() != true && key_language.equals("Korean") != true) {
                            Toast.makeText(getApplicationContext(), "네트워크에 연결되어 있지 않습니다", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        stt_Btn.setImageResource(R.drawable.offbtn);
                        Toast.makeText(getApplicationContext(), "Mic off", Toast.LENGTH_SHORT).show();
                    }
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
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


        find_memo_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final String[] items = new String[arrayList_1.size() + 1];
                final String[] memo = new String[1];

                builder.setTitle("메모저장목록");

                for (int i = 0; i < items.length; i++) {
                    if (i == 0) {
                        items[0] = "작성중인 메모";
                    } else {
                        String date = arrayList_1.get(arrayList_1.size() - i).split("/")[0];
                        items[i] = "메모 " + i + " - " + date;
                    }
                }

                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            dialog.dismiss();
                        } else {
                            memo_idx = arrayList_1.size() - which;
                            if (arrayList_1.isEmpty() != true) {
                                memo[0] = arrayList_1.get(memo_idx).split("/")[1];
                            }
                        }
                    }
                });

                builder.setNeutralButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (memo[0] != null) {
                            stt_TextView.setText(memo[0]);
                        }
                    }
                });

                builder.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (arrayList_1.isEmpty() != true) {
                            arrayList_1.remove(memo_idx);
                        }
                    }
                });

                builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();
            }
        });

        select_language_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("언어선택");

                builder.setItems(languageTable.keys, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, value_language = languageTable.values[which]);
                        text_Language.setText(key_language = languageTable.keys[which].split(" ")[0]);
                        languageTable.setMTTSLanguage(mTTS, key_language);
                        if (isNetworkConnected() != true && key_language.equals("Korean") != true) {
                            Toast.makeText(getApplicationContext(), "네트워크에 연결되어 있지 않습니다", Toast.LENGTH_LONG).show();
                        }
                        dialog.dismiss();
                    }
                });

                builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();
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

                final TextView pitch_txt = Viewlayout.findViewById(R.id.pitch_txt); // txtItem1
                final TextView speed_txt = Viewlayout.findViewById(R.id.speed_txt); // txtItem2
                final TextView volume_txt = Viewlayout.findViewById(R.id.volume_txt);

                popDialog.setIcon(android.R.drawable.presence_audio_online);
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

                /// volume_bar 코드삽입

                volume_bar = Viewlayout.findViewById(R.id.volume_bar);
                volume_bar.setMax(15);

                volume_bar.setProgress(volume_val);
                volume_txt.setText("볼륨 : " + volume_val);

                volume_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        volume_txt.setText("볼륨 : " + progress);
                        volume_val = progress;
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume_val, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                pitch_bar.setProgress(pitch_val);
                speed_bar.setProgress(speed_val);
                volume_bar.setProgress(volume_val);
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
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, 10, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
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
                        }
                    }
                });
                // 창 띄우기
                ad.show();
            }
        });

        tts_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("텍스트 초기화");
                builder1.setMessage("텍스트를 초기화하시겠습니까?");

                builder1.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tts_LinearLayout.removeAllViews();
                        arrayList.clear();
                        text = null;
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

        stt_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("새 메모작성");
                builder1.setMessage("메모를 새로 작성하겠습니까?");

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

        stt_Share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share_Intent = new Intent(Intent.ACTION_SEND);
                share_Intent.addCategory(Intent.CATEGORY_DEFAULT);
                share_Intent.putExtra(Intent.EXTRA_TEXT, stt_TextView.getText().toString().replace(System.getProperty("line.separator"), ""));
                share_Intent.setType("text/plain");
                startActivity(Intent.createChooser(share_Intent, "공유하기"));
                onStop();
            }
        });

        stt_Store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("메모저장");
                builder1.setMessage("메모를 저장하겠습니까?");

                builder1.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = stt_TextView.getText().toString();
                        if (text.isEmpty() != true) {
                            String time = new SimpleDateFormat("yy.MM.dd, HH시mm분").format(new Date(System.currentTimeMillis()));
                            arrayList_1.add(time + "/" + text);
                            Toast.makeText(getApplicationContext(), "저장됨", Toast.LENGTH_SHORT).show();
                        }
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
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager mAudioManager =
                (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI);
                volume_val = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                return true;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI);
                volume_val = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                return true;

            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                return true;
        }
        return false;
    }

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
        String msg = arrayList.size() > 0 ? arrayList.get(arrayList.size() - 1) : null;

        if (micFlag == true && msg != null) stt_Btn.performClick();
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume_val, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        float pitch = (pitch_bar == null) ? pitch_val / 50 : (float) pitch_bar.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (speed_bar == null) ? speed_val / 50 : (float) speed_bar.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);

        //if (arrayList.size() > 0)  mTTS.speak(arrayList.get(arrayList.size() - 1), TextToSpeech.QUEUE_FLUSH, null);
        mTTS.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void speak(String message) {
        if (micFlag == true) stt_Btn.performClick();
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume_val, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

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
                final CharSequence[] items = {"말하기", "공유", "복사", "삭제"};
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // 제목셋팅
                alertDialogBuilder.setTitle("옵션 목록");

                alertDialogBuilder.setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                switch (String.valueOf(items[id])) {
                                    case "말하기":
                                        final String message = textView.getText().toString();
                                        speak(message);
                                        break;

                                    case "공유":
                                        Intent share_Intent = new Intent(Intent.ACTION_SEND);
                                        share_Intent.addCategory(Intent.CATEGORY_DEFAULT);
                                        share_Intent.putExtra(Intent.EXTRA_TEXT, textView.getText());
                                        share_Intent.setType("text/plain");
                                        startActivity(Intent.createChooser(share_Intent, "공유하기"));
                                        onStop();
                                        break;

                                    case "복사":
                                        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                        clipboardManager.setText(textView.getText());
                                        Toast.makeText(getApplicationContext(), "복사되었습니다", Toast.LENGTH_SHORT).show();
                                        break;

                                    case "삭제":
                                        int idx = arrayList.indexOf(textView.getText());
                                        arrayList.remove(idx);
                                        textView.setText("");
                                        break;
                                }
                                dialog.dismiss();
                            }
                        });
                // 다이얼로그 생성
                AlertDialog alertDialog2 = alertDialogBuilder.create();
                // 다이얼로그 보여주기
                alertDialog2.show();
                return false;
            }
        });
    }

    boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo == null ? false : true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        volume_val = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        sharedPreferences = getSharedPreferences(stt_file, Activity.MODE_PRIVATE);
        sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences2 = getSharedPreferences("value_lang", Activity.MODE_PRIVATE);
        sharedPreferences3 = getSharedPreferences("key_lang", Activity.MODE_PRIVATE);
        stt_TextView.setText(sharedPreferences.getString("keyword", ""));
        String json = sharedPreferences1.getString("keyword_1", null);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, value_language = sharedPreferences2.getString("value_language", ""));
        text_Language.setText(key_language = sharedPreferences3.getString("key_language", ""));
        text_Language.setText(key_language = key_language.isEmpty() ? "Korean" : key_language);
        arrayList = new ArrayList<>();
        arrayList_1 = new ArrayList<>();

        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    arrayList_1.add(jsonArray.optString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Cursor cursor = db.rawQuery("select* from mytable;", null);

        while (cursor.moveToNext()) {
            String message = cursor.getString(1);
            arrayList.add(message);
        }

        Log.d("TAG", String.valueOf(arrayList.size()));
        tts_LinearLayout.removeAllViews();

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
        if (clickFlag) {
            stopListen();
            stt_Btn.performClick();
        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume_val, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        mTTS.stop();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String keyword = stt_TextView.getText().toString();
        editor.putString("keyword", keyword);
        editor.apply();

        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        JSONArray jsonArray = new JSONArray();

        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor2.putString("value_language", value_language);
        editor2.apply();

        SharedPreferences.Editor editor3 = sharedPreferences3.edit();
        editor3.putString("key_language", key_language);
        editor3.apply();

        for (int i = 0; i < arrayList_1.size(); i++) {
            jsonArray.put(arrayList_1.get(i));
        }
        if (arrayList_1.isEmpty() != true) {
            editor1.putString("keyword_1", jsonArray.toString());
        } else {
            editor1.putString("keyword_1", null);
        }
        editor1.apply();

        for (int i = 0; i < arrayList.size(); i++) {
            db.execSQL("insert into mytable (message) values('" + arrayList.get(i) + "');");
        }
        arrayList.clear();
        arrayList_1.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            languageTable.setMTTSLanguage(mTTS, key_language);
        }
    }
}


