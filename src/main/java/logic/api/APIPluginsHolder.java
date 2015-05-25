package logic.api;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import logic.SmtpListener;
import logic.interfaces.APIInterface;

public class APIPluginsHolder{
	// メンバ変数
	private String apiType;
	private ArrayList<APIInterface> plugins;
	private static final Logger log = LoggerFactory.getLogger(APIPluginsHolder.class);
	
	//コンストラクタ
	public APIPluginsHolder(String type) {
		this.apiType = type;
	}
	
	//複数のプラグインの中からfunction名に対応するプラグインを返す.
	public APIInterface selectPlugin() {
		plugins = this.holdPlugins();
		
		return this.getPlugin(plugins, apiType);
	}
	
	//cpathにあるjarファイルを読み込んで全Pluginインスタンスを返す.
	private ArrayList<APIInterface> holdPlugins() {
		//プラグイン用の.jarファイルが配置されているパス
		String cpath = System.getProperty("user.dir") + File.separator + "src/main/java/api";
		//使用できるプラグイン全てを得る
		plugins  = this.setPlugins(cpath);
		
		return plugins;
	}
	
	//
	private ArrayList<APIInterface> setPlugins(String cpath){
		ArrayList<APIInterface> plugins = new ArrayList<APIInterface>();
		try {
			File f = new File(cpath);
			String[] files = f.list();
			//
			if(files.length==0)
			 {log.error("not exists file in plugins dir."); return null;}
			for(int i=0;i<files.length;i++) {
				if(files[i].endsWith(".jar")) {
					File file = new File(cpath + File.separator+files[i]);
					JarFile jar = new JarFile(file);
					Manifest mf = jar.getManifest();
					Attributes att = mf.getMainAttributes();
					String cname = att.getValue("Plugin-Class");
					URL url = file.getCanonicalFile().toURI().toURL();
	                URLClassLoader loader = new URLClassLoader(new URL[] { url });
	                Class cobj = loader.loadClass(cname);
	                Class[] ifnames = cobj.getInterfaces();
	                for (int j = 0; j < ifnames.length; j++) {
	                    if (ifnames[j] == APIInterface.class) {
	                    	System.out.println(ifnames[j]);
	                        System.out.println("load..... " + cname);
	                        APIInterface plugin =
	                            (APIInterface)cobj.newInstance();
	                        plugins.add(plugin);
	                        break;
	                    }
	                }
				}
			}
		}catch (Exception ex) {
            ex.printStackTrace();
        }
		return plugins;
	}
	
	//
	private APIInterface getPlugin(ArrayList<APIInterface> plugins, String type) {
		for(int i=0;i<plugins.size();i++) {
			ArrayList<String> pluginTypes = plugins.get(i).getType();
			for(int j=0;j<pluginTypes.size();j++) {
				if(pluginTypes.get(j).equalsIgnoreCase(type))
					return plugins.get(i);
				else 
					continue;
			}
		}
		return null;
	}
}
