package lookingawesomechatserver;



public class LachsUser {

	
	public String uname = "Anonymous";
	public String passwd = "";
	public LachsUserRights lur = LachsUserRights.GUEST;
	
	public LachsUser(String uname, String passwd, LachsUserRights lur)
	{
		
		this.uname = uname;
		this.passwd = passwd;
		this.lur = lur;
	}
	
	
	
}
