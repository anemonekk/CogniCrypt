package crossing.e1.configurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadConfig {

	private String path = "src/main/resources/hashing.js";

	public ReadConfig() {
//		Properties prop = new Properties();
//		InputStream input = null;
//
//		try {
//
//			input = new FileInputStream(Lables.CONFIG_PATH);
//			prop.load(input);
//
//			this.path = prop.getProperty(Lables.CLAFER_PATH);
//
//			System.out.println("path is " + path);
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		} finally {
//			if (input != null) {
//				try {
//					input.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}

	}

	public String getClaferPath() {
		return path;
	}
}