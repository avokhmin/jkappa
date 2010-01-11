package com.plectix.license.server;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import com.plectix.license.client.License;
import com.plectix.license.client.LicenseException;
import com.plectix.license.client.License_V1;
import com.plectix.license.client.SecurityUtil;

public class LicenseGenerator {
    // This key was generated with com.plectix.license.server.KeyGenerator, and corresponds
    // to the public key in com.plectix.license.client.License
    private static final String PRIVATE_KEY_HEX = "30820157020100300d06092a864886f70d0101010500048201413082013d020100024100aff3c80597c966cff656e204837c9a4dbc9e8e9c0c78330ff6445cb5c7456b73937536247890f12a189bf113c035ae70f94059bd2832b25d1c5071f04fb335d902030100010241009445afca1ec5e6b0db1b0e2dd58bdc102421cd756d00a1af12cd3aff2824b4cd73928d66d412db6adfdcdb272af82d40f953a37420684a720300e1b59fa57ecd022100db49ff1254152ae6162f7d43fe0776ef5fddc688b4f13f6c86463fe040a3fb6b022100cd6883926ce63e367ef5bd4732db4899d8254e1d791e160a4143637ce0ce88cb022100b8f726566063e666630a357fd75296887c754553e443a53ab5dba55f5346bdf7022100c8b82d81e34a6656c85f87d3503df9c6e3f1285122aea4a8e6b75c3b864e2c5b022100cda4e146808ce483b83049dcddb73d2b99a7c43162b47f9c4e44e7f73cf111c9";
   
    public static final PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return SecurityUtil.readPrivateKeyFromHexString(PRIVATE_KEY_HEX);
    }

    /**
     * Creates and returns a new license
     * 
     * @throws Exception
     */
    public static final License generateLicense(String username, String apiKey, String jsimKey, String pluginsVersion, long expirationDate) throws Exception {
    	License_V1 license = new License_V1();
    	license.setUsername(username);
    	license.setApiKey(apiKey);
    	license.setJsimKey(jsimKey);
    	license.setPluginsVersion(pluginsVersion);
    	license.setExpirationDate(expirationDate);
    	
    	license.createData(getPrivateKey(), apiKey);
    	return license;
    }


    /**
     * Tests what a client would do with encrypted license data:
     * @param encryptedLicenseData
     * @throws LicenseException 
     */
	private static final void testLicense(String encryptedLicenseData, String username, String apiKey) throws LicenseException {
		License license = License.getLicense(encryptedLicenseData, apiKey);
		int versionNumber = license.getVersionNumber();
		System.out.println("\nLicense Version Number: " + license.getVersionNumber());
		
		if (versionNumber == 1) {
			License_V1 licenseV1 = (License_V1) license;
			System.out.println("isAuthorized: " + licenseV1.isAuthorized(username, apiKey));
			System.out.println("pluginsVersion: " + licenseV1.getPluginsVersion());
			System.out.println("jsimKey: " + licenseV1.getJsimKey());
			System.out.println("expirationDate: " + licenseV1.getExpirationDate());
		}
		
	}

    /**
     * Creates a new license and outputs the license data as a string.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
    	String username = args[0];
    	String apiKey = args[1];
    	String jsimKey = args[2];
    	String pluginsVersion = args[3];                           
    	long expirationDate = Long.parseLong(args[4]);
    	
        License license = generateLicense(username, apiKey, jsimKey, pluginsVersion, expirationDate);
        String encryptedLicenseData = license.getLicenseDataEncrypted();
        
        System.out.println(encryptedLicenseData);
        
        testLicense(encryptedLicenseData, username, apiKey);
    }
}
