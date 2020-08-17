/*
 * Copyright (C) 2019 Yubico.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yubico.yubikit.otp;

import com.yubico.yubikit.keyboard.ChecksumUtils;

import java.util.Arrays;

import javax.annotation.Nullable;

/**
 * <p>Utility methods for creating byte arrays suitable for sending directly
 * to the api of a YubiKey.<p>
 */
class ConfigurationBuilder {
    static public final byte UID_SIZE = 6;    // Size of secret ID field
    static public final byte FIXED_SIZE = 16;        // Max size of fixed field
    static public final byte KEY_SIZE = 16;        // Size of AES key
    static public final byte KEY_SIZE_OATH = 20;      // Size of OATH-HOTP key (key field + first 4 of UID field)
    static public final byte ACC_CODE_SIZE = 6;        // Size of access code to re-program device

    static public final byte CFG_FIXED_OFFS = 0;
    static public final byte CFG_UID_OFFS = FIXED_SIZE;
    static public final byte CFG_KEY_OFFS = CFG_UID_OFFS + UID_SIZE;
    static public final byte CFG_ACC_CODE_OFFS = CFG_KEY_OFFS + KEY_SIZE;
    static public final byte CFG_FIXED_SIZE_OFFS = CFG_ACC_CODE_OFFS + ACC_CODE_SIZE;
    static public final byte CFG_EXT_FLAGS_OFFS = (short) (CFG_FIXED_SIZE_OFFS + 1);
    static public final byte CFG_TKT_FLAGS_OFFS = (short) (CFG_EXT_FLAGS_OFFS + 1);
    static public final byte CFG_CFG_FLAGS_OFFS = (short) (CFG_TKT_FLAGS_OFFS + 1);
    static public final byte CFG_CRC_OFFS = (short) (CFG_CFG_FLAGS_OFFS + 3);
    static public final byte CFG_SIZE = (short) (CFG_CRC_OFFS + 2);

    // Ticket flags

    static public final byte TKTFLAG_TAB_FIRST = 0x01;        // Send TAB before first part
    static public final byte TKTFLAG_APPEND_TAB1 = 0x02;        // Send TAB after first part
    static public final byte TKTFLAG_APPEND_TAB2 = 0x04;        // Send TAB after second part
    static public final byte TKTFLAG_APPEND_DELAY1 = 0x08;        // Add 0.5s delay after first part
    static public final byte TKTFLAG_APPEND_DELAY2 = 0x10;        // Add 0.5s delay after second part
    static public final byte TKTFLAG_APPEND_CR = 0x20;        // Append CR as final character
    static public final byte TKTFLAG_PROTECT_CFG2 = (byte) 0x80;// Block update of config 2 unless config 2 is configured and has this bit set

    static public final byte CFGFLAG_SEND_REF = 0x01;        // Send reference string (0..F) before data
    static public final byte CFGFLAG_PACING_10MS = 0x04;        // Add 10ms intra-key pacing
    static public final byte CFGFLAG_PACING_20MS = 0x08;        // Add 20ms intra-key pacing
    static public final byte CFGFLAG_STATIC_TICKET = 0x20;        // Static ticket generation

    static public final byte CFGFLAG_TICKET_FIRST = 0x02;        // Send ticket first (default is fixed part)
    static public final byte CFGFLAG_ALLOW_HIDTRIG = 0x10;        // Allow trigger through HID/keyboard

    static public final byte CFGFLAG_SHORT_TICKET = 0x02;        // Send truncated ticket (half length)
    static public final byte CFGFLAG_STRONG_PW1 = 0x10;       // Strong password policy flag #1 (mixed case)
    static public final byte CFGFLAG_STRONG_PW2 = 0x40;       // Strong password policy flag #2 (subtitute 0..7 to digits)
    static public final byte CFGFLAG_MAN_UPDATE = (byte) 0x80;// Allow manual (local) update of static OTP

    static public final byte TKTFLAG_OATH_HOTP = 0x40;        // OATH HOTP mode
    static public final byte CFGFLAG_OATH_HOTP8 = 0x02;        // Generate 8 digits HOTP rather than 6 digits
    static public final byte CFGFLAG_OATH_FIXED_MODHEX1 = 0x10;     // First byte in fixed part sent as modhex
    static public final byte CFGFLAG_OATH_FIXED_MODHEX2 = 0x40;     // First two bytes in fixed part sent as modhex
    static public final byte CFGFLAG_OATH_FIXED_MODHEX = 0x50;      // Fixed part sent as modhex
    static public final byte CFGFLAG_OATH_FIXED_MASK = 0x50;      // Mask to get out fixed flags

    static public final byte TKTFLAG_CHAL_RESP = 0x40;       // Challenge-response enabled (both must be set)

    static public final byte CFGFLAG_CHAL_MASK = 0x22;       // Mask to get out challenge type
    static public final byte CFGFLAG_IS_CHAL_RESP = 0x20;       // Flag to indicate if configuration is challenge-response
    static public final byte CFGFLAG_CHAL_YUBICO = 0x20;       // Challenge-response enabled - Yubico OTP mode
    static public final byte CFGFLAG_CHAL_HMAC = 0x22;       // Challenge-response enabled - HMAC-SHA1
    static public final byte CFGFLAG_HMAC_LT64 = 0x04;       // Set when HMAC message is less than 64 bytes


    static public final byte CFGFLAG_CHAL_BTN_TRIG = 0x08;       // Challenge-response operation requires button press
    static public final byte EXTFLAG_SERIAL_BTN_VISIBLE = 0x01;     // Serial number visible at startup (button press)
    static public final byte EXTFLAG_SERIAL_USB_VISIBLE = 0x02;     // Serial number visible in USB iSerial field
    static public final byte EXTFLAG_SERIAL_API_VISIBLE = 0x04;     // Serial number visible via API call

    static public final byte EXTFLAG_USE_NUMERIC_KEYPAD = 0x08;     // Use numeric keypad for digits
    static public final byte EXTFLAG_FAST_TRIG = 0x10;       // Use fast trig if only cfg1 set
    static public final byte EXTFLAG_ALLOW_UPDATE = 0x20;       // Allow update of existing configuration (selected flags + access code)
    static public final byte EXTFLAG_DORMANT = 0x40;       // Dormant configuration (can be woken up and flag removed = requires update flag)

    static public final byte TKTFLAG_UPDATE_MASK = (byte) (TKTFLAG_APPEND_CR);
    static public final byte EXTFLAG_UPDATE_MASK = (byte) (EXTFLAG_SERIAL_API_VISIBLE | EXTFLAG_ALLOW_UPDATE);


    static public final int MODE_AES = 0;
    static public final int MODE_HMAC_SHA1 = 1;
    static public final int MODE_STATIC = 2;

    @Nullable
    private byte[] fixed;
    @Nullable
    private byte[] uid;
    @Nullable
    private byte[] key;
    @Nullable
    private byte[] accCode;
    @Nullable
    private byte[] curAccCode;

    private byte cfgFlags;
    private byte extFlags;
    private byte tktFlags;

    ConfigurationBuilder() {
    }

    /**
     * @param fixed the fixed to set
     */
    public ConfigurationBuilder fixed(byte[] fixed) {
        this.fixed = Arrays.copyOf(fixed, fixed.length > FIXED_SIZE ? FIXED_SIZE : fixed.length);
        return this;
    }

    /**
     * @param uid the uid to set
     */
    public ConfigurationBuilder uid(byte[] uid) {
        this.uid = Arrays.copyOf(uid, uid.length > UID_SIZE ? UID_SIZE : uid.length);
        return this;
    }

    /**
     * @param key the key to set
     */
    public ConfigurationBuilder key(int mode, byte[] key) {
        switch (mode) {
            case MODE_STATIC:
                //static passwords first fill in fixed value, then uid, then the rest is in key
                if (key.length > 0) {
                    fixed(key);
                }
                if (key.length > FIXED_SIZE) {
                    byte[] uid = Arrays.copyOfRange(key, FIXED_SIZE, key.length);
                    uid(uid);
                }
                if (key.length > FIXED_SIZE + UID_SIZE) {
                    int passwordMaxSize = FIXED_SIZE + UID_SIZE + KEY_SIZE;
                    key(MODE_AES, Arrays.copyOfRange(key, FIXED_SIZE + UID_SIZE, Math.min(key.length, passwordMaxSize)));
                }
                break;
            case MODE_HMAC_SHA1:
                // in the hmac-sha1 modes we store the last 4 bytes of the key in the uid
                if (key.length > KEY_SIZE) {
                    this.uid = Arrays.copyOfRange(key, KEY_SIZE, key.length > KEY_SIZE_OATH ? KEY_SIZE_OATH : key.length);
                }
                // besides that fall into default scenario
            default:
                this.key = Arrays.copyOf(key, key.length > KEY_SIZE ? KEY_SIZE : key.length);
                break;
        }
        return this;
    }

    /**
     * @param accCode the accCode to set
     */
    public ConfigurationBuilder accCode(byte[] accCode) {
        this.accCode = Arrays.copyOf(accCode, accCode.length > ACC_CODE_SIZE ? ACC_CODE_SIZE : accCode.length);
        return this;
    }

    /**
     * @param curAccCode the curAccCode to set
     */
    public ConfigurationBuilder curAccCode(byte[] curAccCode) {
        this.curAccCode = Arrays.copyOf(curAccCode, curAccCode.length > ACC_CODE_SIZE ? ACC_CODE_SIZE : curAccCode.length);
        return this;
    }

    /**
     * @param cfgFlags the cfgFlags to set
     */
    public ConfigurationBuilder cfgFlags(byte cfgFlags) {
        this.cfgFlags = cfgFlags;
        return this;
    }

    /**
     * @param extFlags the extFlags to set
     */
    public ConfigurationBuilder extFlags(byte extFlags) {
        this.extFlags = extFlags;
        return this;
    }

    /**
     * @param tktFlags the tktFlags to set
     */
    public ConfigurationBuilder tktFlags(byte tktFlags) {
        this.tktFlags = tktFlags;
        return this;
    }

    public byte[] build() {
        byte[] cfg = new byte[CFG_SIZE + ACC_CODE_SIZE];

        if (fixed != null) System.arraycopy(fixed, 0, cfg, CFG_FIXED_OFFS, fixed.length);
        if (uid != null) System.arraycopy(uid, 0, cfg, CFG_UID_OFFS, uid.length);
        if (key != null) System.arraycopy(key, 0, cfg, CFG_KEY_OFFS, key.length);
        if (accCode != null) System.arraycopy(accCode, 0, cfg, CFG_ACC_CODE_OFFS, accCode.length);
        if (curAccCode != null) System.arraycopy(curAccCode, 0, cfg, CFG_SIZE, curAccCode.length);
        if (fixed != null) cfg[CFG_FIXED_SIZE_OFFS] = (byte) fixed.length;
        cfg[CFG_EXT_FLAGS_OFFS] = extFlags;
        cfg[CFG_TKT_FLAGS_OFFS] = tktFlags;
        cfg[CFG_CFG_FLAGS_OFFS] = cfgFlags;

        short crc = (short) ~ChecksumUtils.calculateCrc(cfg, CFG_SIZE - 2);
        cfg[CFG_CRC_OFFS] = (byte) crc;
        cfg[CFG_CRC_OFFS + 1] = (byte) (crc >> 8);

        return cfg;
    }
}