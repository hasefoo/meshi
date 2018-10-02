package jp.tankofu.meshi.tw;

public class TwApiException extends Exception {
	private static final long serialVersionUID = -7375941362363467509L;
	private String twitterMessage;

	public TwApiException(String statusLine, String twitterMessage) {
		super(statusLine);
		this.twitterMessage = twitterMessage;
	}

	public String getTwitterMessage() {
		return twitterMessage;
	}
}
