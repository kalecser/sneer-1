//package sneer.impl.simulator;
//
//import static sneer.ConversationMenuItem.BY_ALPHABETICAL_ORDER;
//import static sneer.MessageImpl.createFrom;
//import static sneer.MessageImpl.createOwn;
//import static sneer.commons.Clock.now;
//import static sneer.commons.Lists.lastIn;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//import rx.Observable;
//import rx.subjects.ReplaySubject;
//import sneer.Conversation;
//import sneer.ConversationMenuItem;
//import sneer.Message;
//import sneer.Party;
//import sneer.rx.Observed;
//import sneer.rx.ObservedSubject;
//
//public class ConversationSimulator implements Conversation {
//
//	static final ReplaySubject<List<ConversationMenuItem>> menu = ReplaySubject.create();
//
//	private final Party party;
//
//	@SuppressWarnings("unchecked")
//	private final ObservedSubject<List<Message>> messages = ObservedSubject.create((List<Message>)Collections.EMPTY_LIST);
//	@SuppressWarnings("unchecked")
//	private final ObservedSubject<List<ConversationMenuItem>> menuItems = ObservedSubject.create((List<ConversationMenuItem>)Collections.EMPTY_LIST);
//	private final ObservedSubject<Long> mostRecentMessageTimestamp = ObservedSubject.create(0L);
//	private final ObservedSubject<String> mostRecentMessageContent = ObservedSubject.create("");
//	private final ObservedSubject<Long> unreadMessageCount = ObservedSubject.create(4L);
//
//	ConversationSimulator(Party party) {
//		this.party = party;
//		sendMessage("Vai ter festa!!!! Uhuu!!!");
//		simulateReceivedMessage("Onde? Onde?? o0");
//
//		addMenuItem(new ConversationMenuItemSimulator("Send Bitcoins"));
//		addMenuItem(new ConversationMenuItemSimulator("Play Toroidal Go"));
//		addMenuItem(new ConversationMenuItemSimulator("Send my location"));
//		addMenuItem(new ConversationMenuItemSimulator("Send photo"));
//		addMenuItem(new ConversationMenuItemSimulator("Send voice message"));
//		addMenuItem(new ConversationMenuItemSimulator("Play Rock Paper Scissors"));
//		addMenuItem(new ConversationMenuItemSimulator("Play Tic Tac Toe"));
//	}
//
//
//	@Override
//	public Party party() {
//		return party;
//	}
//
//
//	@Override
//	public Observable<List<Message>> messages() {
//		return messages.observed().observable();
//	}
//
//
//	@Override
//	public void sendMessage(String label) {
//		addMessage(createOwn(now(), label));
//		simulateReceivedMessage("Echo: " + label);
//	}
//
//
//	@Override
//	public Observed<Long> mostRecentMessageTimestamp() {
//		return mostRecentMessageTimestamp.observed();
//	}
//
//
//	@Override
//	public Observed<String> mostRecentMessageContent() {
//		return mostRecentMessageContent.observed();
//	}
//
//
//	private void addMessage(Message message) {
//		List<Message> newMessage = messagesWith(message);
//		messages.onNext(newMessage);
//		Message last = lastIn(newMessage);
//		mostRecentMessageTimestamp.onNext(last.timestampReceived());
//		mostRecentMessageContent.onNext(last.label());
//	}
//
//
//	private List<Message> messagesWith(Message message) {
//		List<Message> ret = new ArrayList<Message>(messages.observed().current());
//		ret.add(message);
//		return ret;
//	}
//
//
//	private void simulateReceivedMessage(String content) {
//		addMessage(createFrom(now(), content));
//	}
//
//
//	@Override
//	public Observable<List<ConversationMenuItem>> menu() {
//		return menu;
//	}
//
//
//	private void addMenuItem(ConversationMenuItem menuItem) {
//		List<ConversationMenuItem> newMenuItems = menuItemsWith(menuItem, BY_ALPHABETICAL_ORDER);
//		menuItems.onNext(newMenuItems);
//	}
//
//
//	private List<ConversationMenuItem> menuItemsWith(ConversationMenuItem menuItem, Comparator<ConversationMenuItem> order) {
//		List<ConversationMenuItem> ret = new ArrayList<ConversationMenuItem>(menuItems.observed().current());
//		ret.add(menuItem);
//		Collections.sort(ret, order);
//		return ret;
//	}
//
//
//	@Override
//	public Observable<Long> unreadMessageCount() {
//		return unreadMessageCount.observed().observable();
//	}
//
//
//	@Override
//	public void setBeingRead(boolean isBeingRead) {
//		System.out.println("Conversation.isBeingRead(" + isBeingRead + ")");
//	}
//
//
//	@Override
//	public void sendMessage(String arg0, String label, String arg2, byte[] arg3, Object arg4) {
//		// TODO Auto-generated method stub
//	}
//
//}
