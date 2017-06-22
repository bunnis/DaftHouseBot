import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Bot {
	//bot fo find new houses on daft.ie
	//get the url when you use search on the website, including filters that you might like
	//get emails every x minutes
	//if you get google error and email from google about security you mgiht need to enable access for less secure apps (or you might want to write an oauth and to a pull request :)
	//https://support.google.com/accounts/answer/6010255?hl=en


	private LinkedHashMap<String,House> houses;
	LinkedHashMap<String,House> list_new_houses;
	private String email = "";
	private String email_pass = "";
	private static String url_to_search = "http://www.daft.ie/dublin-city/residential-property-for-rent/dublin-1,dublin-3,dublin-5,dublin-9/?s%5Bmxp%5D=1100";
	private static int time_minutes = 20;//default parsing time, every 20minutes

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url = url_to_search;

		Bot bot = new Bot();

		for(int i=0;true;i++){
			Date now = new Date();
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(now);
			//date of next iteration
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			cal.add(Calendar.MINUTE, time_minutes);
			String timeStampNext = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(cal.getTime());

			System.out.println("Iteration "+i + " "+timeStamp);
			bot.doHouseSearch2(url);
			System.out.println("Next Iteration at "+timeStampNext + "\n");
			
			try {
				Thread.sleep(60*time_minutes*1000);//sleep 20minutes
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}


	private boolean parsePage(String page_url, Boolean current_found_new_houses){

		Boolean found_new_houses = current_found_new_houses;
		try {
			Document doc = Jsoup.connect(page_url).get();

			Elements boxes = doc.select(".box");

			for(Element box : boxes){
				String title = "";
				String price_text = "";
				String url = "http://www.daft.ie";//relative urls
				String id = "";

				Elements search_result_title = box.getElementsByClass("search_result_title_box");

				for(Element t : search_result_title){
					title = t.getElementsByTag("a").text();
					url = url + t.getElementsByTag("a").attr("href");//relative urls

					id = url.split("-")[url.split("-").length -1];
					id = id.substring(0, id.length()-1);//remove /
				}

				Elements price = box.getElementsByClass("price");
				for(Element p : price){
					price_text = checkAndConvertPriceToMonth(p.text());
				}

				list_new_houses.put(id, new House(id,title,price_text,url));

				if(!houses.containsKey(id)){
					found_new_houses = true;
				}

			}



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return found_new_houses;

	}

	private String checkAndConvertPriceToMonth(String price_text) {
		//convert price per week to per month
		if(price_text.toLowerCase().indexOf("week") >= 0){//fyi, it will never start at zero (example=€140 Per week)
			String[] aux = price_text.split(" ");

			int m = Integer.parseInt(aux[0].substring(1, aux[0].length())) * 4;
			price_text = aux[0].charAt(0)+ Integer.toString(m) + " "+aux[1]+" "+ "month";

		} 

		return price_text;
	}

	private void doHouseSearch2(String query_url) {
		// TODO Auto-generated method stub
		Boolean found_new_houses = false;;
		list_new_houses  = new LinkedHashMap<String,House>();




		try {
			Document doc = Jsoup.connect(query_url).get();

			//parse first page
			found_new_houses = parsePage(query_url,found_new_houses);

			//find how many result pages there are
			Elements number_pages = doc.select(".paging").select("a");

			//parse remaining pages (if any)
			for (Element page_num : number_pages){
				String new_page_url = page_num.absUrl("href");
				found_new_houses = parsePage(new_page_url,found_new_houses);
			}

			houses = list_new_houses;//update list

			if(found_new_houses){
				try {
					sendEmail();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				System.out.println("No new houses found, skipping email");
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void sendEmail() throws AddressException, MessagingException {

		/*		Properties props = new Properties();
		props.put("mail.imap.ssl.enable", "true"); // required for Gmail
		props.put("mail.imap.sasl.enable", "true");
		props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
		props.put("mail.imap.auth.login.disable", "true");
		props.put("mail.imap.auth.plain.disable", "true");
		Session session = Session.getInstance(props);
		Store store = session.getStore("imap");
		store.connect("imap.gmail.com", "739641210143-ielm1ha814u5gmbun3rc1uvdjokakrss.apps.googleusercontent.com", "wDBxavKMd0uZvbwCWPOt-XXX");
		 */



		// TODO Auto-generated method stub
		Properties mailServerProperties;
		Session getMailSession;
		MimeMessage generateMailMessage;

		// Step1
		//System.out.println("\n 1st ===> setup Mail Server Properties..");
		mailServerProperties = System.getProperties();
		mailServerProperties.put("mail.smtp.port", "587");
		mailServerProperties.put("mail.smtp.auth", "true");
		mailServerProperties.put("mail.smtp.starttls.enable", "true");
		System.out.println("Mail Server Properties have been setup successfully..");

		// Step2
		//System.out.println("\n\n 2nd ===> get Mail Session..");
		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		generateMailMessage = new MimeMessage(getMailSession);
		generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(this.email));
		generateMailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(this.email));


		generateMailMessage.setSubject("APART - NEW FOUND");


		String emailBody = "Test email by Crunchify.com JavaMail API example. " + "<br><br> Regards, <br>Crunchify Admin";
		emailBody = "";

		for(int i=houses.size()-1; i >=0 ;i--){
			//emailBody = emailBody + "\n"+houses.get+"\n"
		}

		Iterator<House> it = houses.values().iterator();
		while (it.hasNext())
		{
			House currentHouse = it.next();
			emailBody = emailBody + "\n"+currentHouse.getUrl()+"    "+currentHouse.getPrice_text()+"\n";

		}

		//System.out.println(emailBody);

		//System.out.println(emailBody);
		generateMailMessage.setContent(emailBody, "text/plain; charset=UTF-8");
		//System.out.println("Mail Session has been created successfully..");

		// Step3
		//System.out.println("\n\n 3rd ===> Get Session and Send mail");
		Transport transport = getMailSession.getTransport("smtp");

		// Enter your correct gmail UserID and Password
		// if you have 2FA enabled then provide App Specific Password
		transport.connect("smtp.gmail.com", this.email, this.email_pass);
		transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
		transport.close();

		System.out.println("Mail Sent");
	}


	public Bot(){
		houses = new LinkedHashMap<String,House>();
		list_new_houses = new LinkedHashMap<String,House>();
	}

}
