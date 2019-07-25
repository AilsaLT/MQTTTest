package robot.com.myapplication;

public class ListData {
	public static final int SEND = 1;
	public static final int RECEIVE = 2;
	private String fromWho;
	private String toUser;
	private String content;
	private int flag;
	private String time;

	public ListData(String fromWho, String toUser, String content, int flag, String time) {
		this.fromWho = fromWho;
		this.toUser = toUser;
		this.content = content;
		this.flag = flag;
		this.time = time;
	}

	public ListData(String content, int flag, String time) {
		setContent(content);
		setFlag(flag);
		setTime(time);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFromWho() {
		return fromWho;
	}

	public void setFromWho(String fromWho) {
		this.fromWho = fromWho;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
