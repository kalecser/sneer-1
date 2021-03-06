package sneer.android.ipcold;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import sneer.ConversationMenuItem;
import sneer.Message;
import sneer.Sneer;
import sneer.tuples.Tuple;

public class PluginManager {

	private static Map<String, PluginHandler> tupleViewers = new HashMap<String, PluginHandler>();

	private final Func1<List<PluginHandler>, Observable<List<ConversationMenuItem>>> fromSneerPluginInfoList = new Func1<List<PluginHandler>, Observable<List<ConversationMenuItem>>>() { @Override public Observable<List<ConversationMenuItem>> call(List<PluginHandler> apps) {
		return Observable.from(apps)
			.filter(new Func1<PluginHandler, Boolean>() { @Override public Boolean call(PluginHandler handler) {
				return handler.canCompose();
			}})
			.map(new Func1<PluginHandler, ConversationMenuItem>() { @Override public ConversationMenuItem call(final PluginHandler app) {
				return new ConversationMenuItemImpl(PluginManager.this, app);
			}})
			.toList();
	}};

	Context context;
	private final Sneer sneer;


	public PluginManager(Context context, Sneer sneer) {
		this.context = context;
		this.sneer = sneer;
	}


	static byte[] bitmapFor(Drawable icon) {
		Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}


	public boolean isClickable(Message message) {
		return tupleViewers.containsKey(message.tuple().get("message-type"));
	}


	public void doOnClick(Message message) {
		Tuple tuple = message.tuple();
		PluginHandler viewer = tupleViewer((String)tuple.get("message-type"));
		if (viewer == null) {
			throw new RuntimeException("Can't find viewer plugin for message type '" + tuple.get("message-type") + "'");
		}
		context.startActivity(viewer.resume(tuple));
	}


	public PluginHandler tupleViewer(String type) {
		return tupleViewers.get(type);
	}

}
