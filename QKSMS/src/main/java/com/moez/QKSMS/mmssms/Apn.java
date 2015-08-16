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
package com.moez.QKSMS.mmssms;

/**
 * @author shane
 */
public class Apn {
    public String name;
    public String mmsc;
    public String proxy;
    public String port;

    public Apn() {
    }

    public Apn(String name, String mmsc, String proxy, String port) {
        this.name = name;
        this.mmsc = mmsc;
        this.proxy = proxy;
        this.port = port;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Apn)) {
            return false;
        } else {
            Apn apn = (Apn) other;
            return (apn.name == null ? name == null : apn.name.equals(name))
                    && (apn.mmsc == null ? mmsc == null : apn.mmsc.equals(mmsc))
                    && (apn.proxy == null ? proxy == null : apn.proxy.equals(proxy))
                    && (apn.port == null ? port == null : apn.port.equals(port));
        }
    }

    @Override
    public int hashCode() {
        // http://stackoverflow.com/questions/113511/hash-code-implementation
        int result = 7;
        result = 37 * result + (name == null ? 0 : name.hashCode());
        result = 37 * result + (mmsc == null ? 0 : mmsc.hashCode());
        result = 37 * result + (proxy == null ? 0 : proxy.hashCode());
        result = 37 * result + (port == null ? 0 : port.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format("{name:%s, mmsc:%s, proxy:%s, port:%s}", name, mmsc, proxy, port);
    }
}
