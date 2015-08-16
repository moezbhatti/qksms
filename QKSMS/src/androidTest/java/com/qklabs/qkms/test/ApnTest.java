/*
 * Copyright (C) 2015 QK Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qklabs.qkms.test;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.moez.QKSMS.mmssms.Apn;
import com.moez.QKSMS.mmssms.ApnUtils;

import java.util.Arrays;
import java.util.List;

public class ApnTest extends InstrumentationTestCase {
    Context context;

    public void setUp() {
        context = getInstrumentation().getContext();
    }

    public void testQueryPortInsteadOfMmsPort() {
        List<Apn> actual = ApnUtils.query(context, "310", "012");
        List<Apn> expected = Arrays.asList(
                new Apn("Verizon", "http://mms.vtext.com/servlets/mms", null, "80")
        );
        assertEquals(expected, actual);
    }

    public void testQueryMultipleMatching() {
        List<Apn> actual = ApnUtils.query(context, "310", "240");
        List<Apn> expected = Arrays.asList(
                new Apn("T-Mobile US 240", "http://mms.msg.eng.t-mobile.com/mms/wapenc", null, null),
                new Apn("MetroPCS 240", "http://metropcs.mmsmvno.com/mms/wapenc", null, null)
        );
        assertEquals(expected, actual);
    }

    public void testQueryLeadingZero() {
        List<Apn> actual = ApnUtils.query(context, "724", "019");
        List<Apn> expected = Arrays.asList(
                new Apn("Vivo MMS", "http://termnat.vivomms.com.br:8088/mms", "200.142.130.104", "80")
        );
        assertEquals(expected, actual);
    }

    public void testQueryNonLeadingZeroDoesntMatchLeadingZero() {
        List<Apn> actual = ApnUtils.query(context, "724", "19");
        List<Apn> expected = Arrays.asList(
                new Apn("Vivo MMS", "http://termnat.vivomms.com.br:8088/mms", "200.142.130.104", "80")
        );
        assertEquals(expected, actual);
    }

    public void testQueryBoostMobile() {
        List<Apn> actual = ApnUtils.query(context, "311", "870");
        List<Apn> expected = Arrays.asList(
                new Apn("Boost Mobile", "http://mm.myboostmobile.com", "68.28.31.7", "80")
        );
        assertEquals(expected, actual);
    }

    public void test311480Verizon() {
        List<Apn> actual = ApnUtils.query(context, "311", "480");
        List<Apn> expected = Arrays.asList(
                new Apn("Verizon", "http://mms.vtext.com/servlets/mms", null, null)
        );
        assertEquals(expected, actual);
    }

    public void test310260TMobile() {
        List<Apn> actual = ApnUtils.query(context, "310", "260");
        Apn expected = new Apn("T-Mobile", "http://mms.msg.eng.t-mobile.com/mms/wapenc", null, null);
        assertTrue(actual.contains(expected));
    }

    public void test310120Sprint() {
        List<Apn> actual = ApnUtils.query(context, "310", "120");
        List<Apn> expected = Arrays.asList(
                new Apn("Sprint", "http://mms.sprintpcs.com", "68.28.31.7", "80")
        );
        assertEquals(expected, actual);
    }

    public void test26806TMN() {
        List<Apn> actual = ApnUtils.query(context, "268", "06");
        List<Apn> expected = Arrays.asList(
                new Apn("TMN", "http://mmsc/", "10.111.2.16", "8080")
        );
        assertEquals(expected, actual);
    }

    public void test311490VirginMobile() {
        List<Apn> actual = ApnUtils.query(context, "311", "490");
        Apn expected = new Apn("Virgin Mobile", "http://mmsc.vmobl.com:8088/mms?", "205.239.233.136", "81");
        assertTrue(actual.contains(expected));
    }

    public void test23410O2() {
        List<Apn> actual = ApnUtils.query(context, "234", "10");
        List<Apn> expected = Arrays.asList(
                new Apn("O2", "http://mmsc.mms.o2.co.uk:8002", "82.132.254.1", "8080"),
                new Apn("TESCO", "http://mmsc.mms.o2.co.uk:8002", "193.113.200.195", "8080")
        );
        assertEquals(expected, actual);
    }

    public void testSolaveiBlank() {
        List<Apn> actual = ApnUtils.query(context, "310", "260");
        Apn expected = new Apn("Solavei", "http://solavei.mmsmvno.com/mms/wapenc", null, null);
        assertTrue(actual.contains(expected));
    }

    public void testNet10() {
        List<Apn> actual = ApnUtils.query(context, "310", "410");
        Apn expected = new Apn("net10", "http://mms.tracfone.com", null, null);
        assertTrue(actual.contains(expected));
    }

    public void testATTApnSettings() {
        List<Apn> actual = ApnUtils.query(context, "310", "410");
        Apn expected1 = new Apn("AT&T 4G", "http://mmsc.mobile.att.net", "proxy.mobile.att.net", "80");
        Apn expected2 = new Apn("AT&T 3G/WAP", "http://mmsc.cingular.com/", "wireless.cingular.com", "80");
        assertTrue(actual.contains(expected1));
        assertTrue(actual.contains(expected2));
    }

    public void testGrameenPhone() {
        List<Apn> actual = ApnUtils.query(context, "470", "01");
        Apn expected = new Apn("Grameenphone", "http://mmsc.grameenphone.com/servlets/mms", null, null);
        assertTrue(actual.contains(expected));
    }

    public void testAirtel() {
        List<Apn> actual = ApnUtils.query(context, "470", "07");
        Apn expected = new Apn("Airtel", "http://100.1.201.171:10021/mmsc", "100.1.201.172", "8799");
        assertTrue(actual.contains(expected));
    }

    public void testSMARTFREN() {
        List<Apn> actual = ApnUtils.query(context, "510", "09");
        Apn expected1 = new Apn("SMARTFREN 0881, 0882", "http://mmsc-jkt.smart-telecom.co.id", "10.17.27.250", "8080");
        Apn expected2 = new Apn("SMARTFREN 0887, 0888, 0889", "http://mmsc2.smartfren.com", "10.17.27.250", "8080");
        assertTrue(actual.contains(expected1));
        assertTrue(actual.contains(expected2));
    }

    public void testStraightTalk() {
        List<Apn> actual = ApnUtils.query(context, "310", "410");
        Apn expected = new Apn("Straight Talk", "http://mms-tf.net", "mms3.tracfone.com", "80");
        assertTrue(actual.contains(expected));
    }

    public void testCricket310410() {
        List<Apn> actual = ApnUtils.query(context, "310", "410");
        Apn expected = new Apn("Cricket", "http://mmsc.aiowireless.net", "proxy.aiowireless.net", "80");
        assertTrue(actual.contains(expected));
    }

    public void testCricket310150() {
        List<Apn> actual = ApnUtils.query(context, "310", "150");
        Apn expected = new Apn("Cricket", "http://mmsc.aiowireless.net", "proxy.aiowireless.net", "80");
        assertTrue(actual.contains(expected));
    }

    public void testTIGO() {
        List<Apn> actual = ApnUtils.query(context, "640", "02");
        Apn expected = new Apn("TIGO", "http://mms", "10.16.17.12", "8888");
        assertTrue(actual.contains(expected));
    }

    public void testDSTCom() {
        List<Apn> actual = ApnUtils.query(context, "528", "11");
        Apn expected = new Apn("DSTCom", "http://mms.dst.com.bn/mmsc", "10.100.6.101", "3130");
        assertTrue(actual.contains(expected));
    }

    public void testVodacomMozambique() {
        List<Apn> actual = ApnUtils.query(context, "643", "04");
        Apn expected = new Apn("Vodacom", "http://mms.vm.co.mz", "10.201.47.14", "9201");
        assertTrue(actual.contains(expected));
    }

    public void testVirginMobileMms() {
        List<Apn> actual = ApnUtils.query(context, "310", "000");
        Apn expected = new Apn("Virgin Mobile", "http://mmsc.vmobl.com:8088/mms?", "205.239.233.136", "81");
        assertTrue(actual.contains(expected));
    }

    public void testH2O() {
        List<Apn> actual = ApnUtils.query(context, "310", "410");
        Apn expected = new Apn("H2O", "http://mmsc.cingular.com", "66.209.11.33", "80");
        assertTrue(actual.contains(expected));
    }

    public void testSpeakOut() {
        List<Apn> actual = ApnUtils.query(context, "302", "720");
        Apn expected = new Apn("SpeakOut", "http://mms.gprs.rogers.com", "mmsproxy.rogers.com", "80");
        assertTrue(actual.contains(expected));
    }

    public void testApnEquals() {
        assertEquals(new Apn(null, null, null, null), new Apn(null, null, null, null));
        assertEquals(new Apn("Test", null, null, null), new Apn("Test", null, null, null));
        assertEquals(new Apn("A", "B", null, null), new Apn("A", "B", null, null));
        assertEquals(new Apn("A", "B", "C", null), new Apn("A", "B", "C", null));
        assertEquals(new Apn("A", "B", "C", "D"), new Apn("A", "B", "C", "D"));
        assertEquals(new Apn("", "B", "", "D"), new Apn("", "B", "", "D"));
    }

    public void testApnEmptyConstructor() {
        assertEquals(new Apn(), new Apn(null, null, null, null));
    }

    public void testApnManuallyConstructed() {
        Apn manual;
        manual = new Apn();
        manual.name = "A"; manual.mmsc = "B"; manual.proxy = "C"; manual.port = "D";
        assertEquals(new Apn("A", "B", "C", "D"), manual);

        manual = new Apn();
        manual.name = "A"; manual.proxy = "C";
        assertEquals(new Apn("A", null, "C", null), manual);
    }
}
