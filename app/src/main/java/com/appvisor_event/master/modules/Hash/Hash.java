package com.appvisor_event.master.modules.Hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by bsfuji on 2017/04/04.
 */

public class Hash
{
    public static String md5(File file)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);

            byte[] buffer = new byte[4096];

            int read;
            while ((read = is.read(buffer)) > 0)
            {
                digest.update(buffer, 0, read);
            }

            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            output = String.format("%32s", output).replace(' ', '0');

            return output;
        }
        catch (Exception exeption) {}

        return null;
    }
}
