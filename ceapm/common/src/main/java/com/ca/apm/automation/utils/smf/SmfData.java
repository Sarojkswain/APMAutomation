/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.automation.utils.smf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Encapsulates data for a single SMF record.
 */
public class SmfData {
    protected byte[] data;

    /**
     * Constructor.
     *
     * @param data Raw SMF record data.
     */
    public SmfData(byte[] data) {
        assert data != null;
        assert data.length > 0;

        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    /**
     * Factory that creates a {@link SmfData} instance from a file containing the hex
     * representation of its data.
     *
     * <p>The whole contents of the file will be read in and each character pair will be interpreted
     * as a hex value. Any trailing (odd) character will be ignored.
     *
     * @param file Source file.
     * @return {@link SmfData} instance.
     * @throws IOException If any I/O issues are encountered during processing of the file.
     */
    public static SmfData fromFile(Path file) throws IOException {
        final byte[] rawData = Files.readAllBytes(file);
        return fromRawHexData(rawData);
    }

    /**
     * Factory that creates a {@link SmfData} instance from a hex representation of its data.
     *
     * <p>Each pair of bytes within the input array will be interpreted as a hex value. Any trailing
     * (odd) byte will be ignored.
     *
     * @param rawData Array with hex representation of the data bytes.
     * @return {@link SmfData} instance.
     */
    public static SmfData fromRawHexData(byte[] rawData) {
        return new SmfData(hexToByte(rawData));
    }

    /**
     * Utility method to convert a byte array containing hex representations of bytes into actual
     * bytes.
     *
     * @param hex Input array with hex representation of bytes.
     * @return Array with converted actual bytes.
     */
    private static byte[] hexToByte(byte[] hex) {
        assert hex != null;
        assert hex.length > 0;

        int bytes = hex.length / 2;
        final byte[] output = new byte[bytes];

        for (int i = 0; i < bytes; ++i) {
            output[i] = (byte) ((Character.digit(hex[i*2], 16) << 4)
                + Character.digit(hex[i*2+1], 16));
        }

        return output;
    }
}
