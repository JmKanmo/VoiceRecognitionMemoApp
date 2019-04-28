package com.junmo.study;

import android.app.Activity;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

public class LanguageTable extends Activity {
    HashMap<String, String> hashMap = new HashMap<String, String>() {
        {
            put("Korean (한국)", "ko-KR");
            put("Chinese (中國)", "zh");
            put("Vietnamese (VietNam)", "vi-VN");
            put("Uzbek (Uzbekistan)", "uz-UZ");
            put("Ukrainian (Ukraine)", "uk-UA");
            put("Russian (Russia)", "ru-RU");
            put("Turkish (Turkey)", "tr-TR");
            put("Tagalog (Philippines)", "tl-PH");
            put("Thai (Thailand)", "th-TH");
            put("Hindi (India)", "hi-IN");
            put("Swedish (Sweden)", "sv-SE");
            put("Slovak (Slovakia)", "sk-SK");
            put("Romanian (Romania)", "ro-RO");
            put("Portuguese (Portugal)", "pt-PT");
            put("Polish (Poland)", "pl-PL");
            put("Dutch (Netherlands)", "nl-NL");
            put("Malay (Malaysia)", "ms-MY");
            put("Japanese (日本)", "ja-JP");
            put("Italian (Italy)", "it-IT");
            put("Icelandic (Iceland)", "is-IS");
            put("Indonesian (Indonesia)", "id-ID");
            put("Hungarian (Hungary)", "hu-HU");
            put("Croatian (Croatia)", "hr-HR");
            put("Hebrew (Israel)", "he-IL");
            put("French (France)", "fr-FR");
            put("Spanish (Spain)", "es-ES");
            put("English (South Africa)", "en-ZA");
            put("English (US)", "en-US");
            put("English (Philippines)", "en-PH");
            put("English (NewZealand)", "en-NZ");
            put("English (Ireland)", "en-IE");
            put("English (UK)", "en-GB");
            put("English (Canada)", "en-CA");
            put("English (Australia)", "en-AU");
            put("Greek (Greece)", "el-GR");
            put("German (Germany)", "de-DE");
            put("Arabic (U.A.E.)", "ar-AE");
        }
    };

    String[] keys = new String[hashMap.size()];
    String[] values = new String[hashMap.size()];

    LanguageTable() {
        TreeMap<String, String> tm = new TreeMap<String, String>(hashMap);
        Set<String> keyset = hashMap.keySet();
        Iterator<String> keyiterator = tm.keySet().iterator(); //키값 오름차순 정렬
        // 	Iterator<string> keyiterator = tm.descendingKeySet().iterator(); //키값 내림차순 정렬

        for (int i = 0; keyiterator.hasNext(); i++) {
            keys[i] = keyiterator.next();
            values[i] = tm.get(keys[i]);
        }
        //hashMap.keySet().toArray(keys);
        //hashMap.values().toArray(values);
    }

    int setMTTSLanguage(TextToSpeech mTTS, String language) {
        int result;
        switch (language) {
            case "Korean":
                result = mTTS.setLanguage(Locale.KOREAN);
                break;
            case "Chinese":
                result = mTTS.setLanguage(Locale.CHINESE);
                break;
            case "Thai":
                result = mTTS.setLanguage(Locale.TAIWAN);
                break;
            case "Japanese":
                result = mTTS.setLanguage(Locale.JAPANESE);
                break;
            case "Italian":
                result = mTTS.setLanguage(Locale.ITALIAN);
                break;
            case "French":
                result = mTTS.setLanguage(Locale.FRANCE);
                break;
            case "English (US)":
                result = mTTS.setLanguage(Locale.US);
                break;
            case "English (UK)":
                result = mTTS.setLanguage(Locale.UK);
                break;
            case "English (Canada)":
                result = mTTS.setLanguage(Locale.CANADA);
                break;
            case "German":
                result = mTTS.setLanguage(Locale.GERMAN);
                break;
            default:
                result = mTTS.setLanguage(Locale.getDefault());
                break;
        }
        return result;
    }
}
