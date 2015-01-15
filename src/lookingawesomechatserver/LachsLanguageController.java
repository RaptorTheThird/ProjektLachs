package lookingawesomechatserver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LachsLanguageController {

	private Map<String,Locale> supportedLanguages;
	private ResourceBundle translation;
	
	public LachsLanguageController(String language)
	{
		init(language);
	}
	
	private boolean init(String language)
	{
		language = language.toLowerCase();
		ClassLoader loader = null;
		
		try {			
			File file = new File("./resources/lang/");
			URL[] urls = {file.toURI().toURL()};
			loader = new URLClassLoader(urls);			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		supportedLanguages = new HashMap<String,Locale>();
		supportedLanguages.put("deutsch", Locale.GERMAN);
		supportedLanguages.put("german", Locale.GERMAN);
		supportedLanguages.put("english", Locale.ENGLISH);
		supportedLanguages.put("sarkasmus",new Locale("sc","Sarkasmus","sc"));
		supportedLanguages.put("1337",new Locale("13","1337","13"));
		
		try {
		
			translation = ResourceBundle.getBundle("languages", supportedLanguages.get(language),loader);
			return true;
			
		} catch (Exception e) {
			
			System.out.println("Languagepack "+language+" is not supported yet!");
			translation = ResourceBundle.getBundle("languages", supportedLanguages.get("english"),loader);
			return false;
		}	
	}
	
	public boolean changeLanguage(String language)
	{	
		System.out.println("change language");
		return init(language);				
	}
	
	public String translate(String keyword)
	{
		return translation.getString(keyword);
	}
	
}
