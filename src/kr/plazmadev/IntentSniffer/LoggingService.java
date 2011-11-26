package kr.plazmadev.IntentSniffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.os.Binder;
import android.os.IBinder;

public class LoggingService extends Service {
	
	/**
	 * 출력한 모든 소스의 목록
	 */
	public Set<String> mReporting = new HashSet<String>();
	
	/**
	 * 알려진 액션들.</br>
	 * 대부분은 SDK에서 로드돼지 않음.
	 */
	public String[] mKnownBroadcastActions = {
			"android.provider.Telephony.SECRET_CODE",
			"android.provider.Telephony.SPN_STRINGS_UPDATED",
			"com.android.mms.transaction.MessageStatusReceiver.MESSAGE_STATUS_RECEIVED",
			"com.android.mms.transaction.MESSAGE_SENT",
			"android.intent.action.ANR", "android.intent.action.stk.command",
			"android.intent.action.stk.session_end",
			"com.android.im.SmsService.SMS_STATUS_RECEIVED" };

	/**
	 * 브로드캐스트를 발송하는 클래스들. 아직 완성되지 않음(이제 시작).</br>
	 * 이 클래스들은 SDK API에는 존재하지 않으며, </br>
	 * 디바이스 런타임에서 클래스 리플렉션으로 가져오게 됨.
	 */
	public String[] actionHarboringClasses = {
			"android.content.Intent",
			"android.bluetooth.BluetoothIntent",
			"android.bluetooth.BluetoothA2dp",
			// Application specific, and would be dangerous to load (as their
			// static initializer might run etc.)
			// "com.android.mms.transaction.MessageStatusReceiver",
			// "com.android.mms.transaction.SmsReceiverService",
			// "com.android.internal.telephony.gsm.stk.AppInterface",
			"com.android.internal.location.GpsLocationProvider",
			"com.android.internal.telephony.TelephonyIntents",
			"android.provider.Telephony.Intents",
			"android.proivder.Contacts.Intents",
			"com.android.mms.util.RateController",
			"android.media.AudioManager", "android.net.wifi.WifiManager",
			"android.telephony.TelephonyManager",
			"android.appwidget.AppWidgetManager",
			"android.net.ConnectivityManager" };

	public String[] mKnownSchemes = { ContentResolver.SCHEME_ANDROID_RESOURCE,
			ContentResolver.SCHEME_CONTENT, ContentResolver.SCHEME_FILE,
			"http", "https", "mailto", "wtai", "tel", "imap", "pop3", "local",
			"geo", "", "ftp", "svn", "ssh", "im", "package", "voicemail",
			"about", "mmsto", "mms", "smsto", "sms", "market",
			"google.streetview", "rtsp", "android_secret_code", "lastfm" };

	public String[] mKnownMimeTypes = { "*", "vnd.android.cursor.dir",
			"vnd.android.cursor.item", "video", "audio", "application", "text",
			"image", "vnd.android-dir" }; // * is the only important one

	public String[] mKnownCategories = { "android.intent.category.HOME",
			"android.intent.category.LAUNCHER", "video", "audio",
			"application", "text", "image", "vnd.android-dir" };

	/** 현재 리시버 */
	public List<BroadcastReceiver> mReceivers = new ArrayList<BroadcastReceiver>();

	// keeping track
	public int mNumReflected = 0;
	public int mNumDug = 0;

	
	/**
	 * 현재 수신된 모든 인텐츠가 저장돼는 해시맵. 소스는 자연어 String임.
	 * 모든 인텐츠를 저장하는 것은 용량 낭비이며, 일부 인텐츠는 매우 클 수 있음.
	 */
	private final HashMap<String, Collection<FilterComparison>> mReceivedIntents = new HashMap<String, Collection<FilterComparison>>();

	public static final String TAG = "IntentSniffer";

	// work
	public static final String RECENT_SOURCE = "recent tasks";
	public static final String ACTION_ONLY_SOURCE = "known action";
	public static final String ACTION_AND_DATA_SOURCE = "known action and data";
	public static final String ACTION_AND_DATA_TYPE_SOURCE = "known action and data and type";
	// don't work
	public static final String WILD_ACTION_SOURCE = "wild action";
	public static final String WILD_ACTION_AND_DATA_SOURCE = "wild action known data";
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	LocalBinder mBinder = new LocalBinder();
	
	/**
     * 커스텀 바인더 클래스
     */
    class LocalBinder extends Binder {
	    LoggingService getService() {
            return LoggingService.this;
        }
    };
}
