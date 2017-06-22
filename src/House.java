
public class House {

	private String id;
	private String title;
	private String price_text;
	private String url;
	
	@Override
	public String toString() {
		return "House [id=" + id + ", title=" + title + ", price_text=" + price_text + ", url=" + url + "]";
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getPrice_text() {
		return price_text;
	}

	public String getUrl() {
		return url;
	}

	public House(String id, String title, String price_text, String url) {
		this.id = id;
		this.title = title;
		this.price_text = price_text;
		this.url = url;
	}

	
}
