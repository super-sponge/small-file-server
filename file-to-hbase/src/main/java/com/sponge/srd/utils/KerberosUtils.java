package com.sponge.srd.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class KerberosUtils {
    private static final Logger LOG = LoggerFactory.getLogger(KerberosUtils.class);
    public static void Kerberos(Configuration conf) {
        PropertiesConfiguration props = Props.getProperties();
        boolean kerberos = props.getBoolean(Constant.KERBEROS_ENABLE);
        if (kerberos) {
            String kdcConf = props.getString(Constant.JAVA_SECURITY_KRB5_CONF);
            if (kdcConf != null ) {
                LOG.info("KRB5_CONF file " + kdcConf);
                System.setProperty(Constant.JAVA_SECURITY_KRB5_CONF, kdcConf);
            } else {
                String kdcServer = props.getString(Constant.JAVA_SECURITY_KRB5_KDC);
                String kdcRealm = props.getString(Constant.JAVA_SECURITY_KRB5_REALM);

                if (kdcServer == null || kdcRealm == null ) {
                    LOG.error("Please check api-server.properties :  set java.security.krb5.conf  " +
                            "or set java.security.krb5.kdc and java.security.krb5.realm");
                    return ;
                }
                LOG.info(Constant.JAVA_SECURITY_KRB5_KDC + " = " + kdcServer);
                LOG.info(Constant.JAVA_SECURITY_KRB5_REALM + " = " + kdcRealm);

                System.setProperty(Constant.JAVA_SECURITY_KRB5_KDC, kdcServer);
                System.setProperty(Constant.JAVA_SECURITY_KRB5_REALM, kdcRealm);
            }

            String keytab = props.getString(Constant.KEYTAB_FILE);
            String principal = props.getString(Constant.KERBEROS_PRINCIPAL);

            LOG.info(Constant.KEYTAB_FILE + " = " + keytab);
            LOG.info(Constant.KERBEROS_PRINCIPAL + " = " + principal);

            conf.set(Constant.KEYTAB_FILE, keytab);
            conf.set(Constant.KERBEROS_PRINCIPAL, principal);

            conf.set("hadoop.security.authentication" , "Kerberos" );

            UserGroupInformation.setConfiguration(conf);
            try {
                LOG.info("Kerberos login ...");
                UserGroupInformation.loginUserFromKeytab(principal, keytab);
                LOG.info("Kerberso login succed!");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return  ;
            }
        }

    }
}
