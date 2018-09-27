package com.ca.apm.systemtest.fld.common.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.files.InsertPoint.Location;
import com.ca.apm.systemtest.fld.common.files.UpdateLinesOperation.OneLineUpdate;
import com.ca.apm.systemtest.fld.common.files.UpdateLinesOperation.UpdateMethod;

public class FileUtilsTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtilsTest.class);

    @Test
    public void test_checkInsertLinesAlreadyIncluded()
    {
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, -1, Arrays.asList(new String[] { "aaa", "bbb", "ccc" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(null, 0, Arrays.asList(new String[] { "aaa", "bbb", "ccc" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 0, null, Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[0], 0, Arrays.asList(new String[] { "aaa", "bbb", "ccc" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 0, Arrays.asList(new String[0]), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 0, Arrays.asList(new String[] { "aaa", "bbb" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 0, Arrays.asList(new String[] { "aaa", "bbb", "ccc" }), null));


        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }, 0, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }, 4, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 0, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 3, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 2, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "ccc", "ddd", "eee" }, 2, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "ccc", "ddd", "eee" }, 4, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbb", "ccc", "ddd" }, 1, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbb", "ccc", "ddd" }, 4, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbb", "ccc", "ddd" }, 3, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb" }, 0, Arrays.asList(new String[] { "aaa", "bbb", "aaa", "bbb", "aaa" }), Location.After));
        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb" }, 2, Arrays.asList(new String[] { "aaa", "bbb", "aaa", "bbb", "aaa" }), Location.After));
        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb" }, 2, Arrays.asList(new String[] { "aaa", "bbb", "aaa", "bbb", "aaa" }), Location.Before));
        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb" }, 4, Arrays.asList(new String[] { "aaa", "bbb", "aaa", "bbb", "aaa" }), Location.Before));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb" }, 1, Arrays.asList(new String[] { "aaa", "bbb", "aaa", "bbb", "aaa" }), Location.Before));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb" }, 3, Arrays.asList(new String[] { "aaa", "bbb", "aaa", "bbb", "aaa" }), Location.Before));


        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaX", "bbb", "ccc", "ddd", "eee" }, 0, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaX", "bbb", "ccc", "ddd", "eee" }, 4, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaX", "bbb", "ccc" }, 0, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaX", "bbb", "ccc" }, 2, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "ccX", "ddd", "eee" }, 2, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "ccX", "ddd", "eee" }, 4, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbX", "ccc", "ddd" }, 1, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbX", "ccc", "ddd" }, 3, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));


        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }, 0, Arrays.asList(new String[] { "aaX", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }, 4, Arrays.asList(new String[] { "aaX", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 0, Arrays.asList(new String[] { "aaX", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 2, Arrays.asList(new String[] { "aaX", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "ccc", "ddd", "eee" }, 2, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddX", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "ccc", "ddd", "eee" }, 4, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddX", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbb", "ccc", "ddd" }, 1, Arrays.asList(new String[] { "aaa", "bbX", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbb", "ccc", "ddd" }, 3, Arrays.asList(new String[] { "aaa", "bbX", "ccc", "ddd", "eee" }), Location.Before));


        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }, 4, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }, 5, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 3, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "bbb", "ccc" }, 1, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "ccc", "ddd", "eee" }, 3, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "ccc", "ddd", "eee" }, 1, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbb", "ccc", "ddd" }, 3, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "bbb", "ccc", "ddd" }, 1, Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ddd", "eee" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "aaa", "aaa", "aaa", "aaa" }, 1, Arrays.asList(new String[] { "aaa", "aaa", "aaa", "aaa", "aaa" }), Location.After));
        assertTrue(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "aaa", "aaa", "aaa", "aaa" }, 5, Arrays.asList(new String[] { "aaa", "aaa", "aaa", "aaa", "aaa" }), Location.Before));

        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "aaa", "aaa" }, 3, Arrays.asList(new String[] { "aaa", "aaa", "aaa", "aaa", "aaa" }), Location.After));
        assertFalse(FileUtils.checkInsertLinesAlreadyIncluded(new String[] { "aaa", "aaa", "aaa" }, 1, Arrays.asList(new String[] { "aaa", "aaa", "aaa", "aaa", "aaa" }), Location.Before));
    }

    @Test
    public void test_insertIntoFile() throws IOException, URISyntaxException
    {
        File src = getFile("src.txt");
        File tgt = createTempFile("test_insertIntoFile_1_");
        LOGGER.info("test_insertIntoFile():: source file: src = " + src);
        LOGGER.info("test_insertIntoFile():: target file: tgt = " + tgt);
        List<String> lines;

        FileUtils.insertIntoFile(src, tgt, new String[] { "XXX" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "XXX", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "XXX" }, new InsertPoint[] { InsertPoint.after("^ccc") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "XXX", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "aaa" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "ccc" }, new InsertPoint[] { InsertPoint.after("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "ccc" }, new InsertPoint[] { InsertPoint.before("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "aaa" }, new InsertPoint[] { InsertPoint.before("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));


        FileUtils.insertIntoFile(src, tgt, new String[] { "XXX", "YYY", "ZZZ" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "XXX", "YYY", "ZZZ", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "XXX", "YYY", "ZZZ" }, new InsertPoint[] { InsertPoint.after("^ccc") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "XXX", "YYY", "ZZZ", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "aaa", "aaa", "aaa" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "aaa", "aaa", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "ccc", "ccc", "ccc" }, new InsertPoint[] { InsertPoint.after("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "ccc", "ccc", "ccc" }, new InsertPoint[] { InsertPoint.before("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFile(src, tgt, new String[] { "aaa", "AAA" }, new InsertPoint[] { InsertPoint.before("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "AAA", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
    }

    @Test
    public void test_insertIntoFileSkipIfAlreadyIncludes() throws IOException, URISyntaxException
    {
        File src = getFile("src.txt");
        File tgt = createTempFile("test_insertIntoFileSkipIfAlreadyIncludes_1_");
        LOGGER.info("test_insertIntoFileSkipIfAlreadyIncludes_1():: source file: src = " + src);
        LOGGER.info("test_insertIntoFileSkipIfAlreadyIncludes_1():: target file: tgt = " + tgt);
        List<String> lines;

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "XXX" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "XXX", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "XXX" }, new InsertPoint[] { InsertPoint.after("^ccc") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "XXX", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "aaa" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "ccc" }, new InsertPoint[] { InsertPoint.after("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "ccc" }, new InsertPoint[] { InsertPoint.before("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "aaa" }, new InsertPoint[] { InsertPoint.before("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "lll" }, new InsertPoint[] { InsertPoint.after("^ll*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "lll" }, new InsertPoint[] { InsertPoint.before("^ll*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));


        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "XXX", "YYY", "ZZZ" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "XXX", "YYY", "ZZZ", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt, tgt, new String[] { "XXX", "YYY", "ZZZ" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "XXX", "YYY", "ZZZ", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "XXX", "YYY", "ZZZ" }, new InsertPoint[] { InsertPoint.after("^ccc") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "XXX", "YYY", "ZZZ", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt, tgt, new String[] { "XXX", "YYY", "ZZZ" }, new InsertPoint[] { InsertPoint.after("^ccc") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "XXX", "YYY", "ZZZ", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "aaa", "aaa", "aaa" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "aaa", "aaa", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt, tgt, new String[] { "aaa", "aaa", "aaa" }, new InsertPoint[] { InsertPoint.after("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "aaa", "aaa", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "ccc", "ccc", "ccc" }, new InsertPoint[] { InsertPoint.after("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt, tgt, new String[] { "ccc", "ccc", "ccc" }, new InsertPoint[] { InsertPoint.after("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "ccc", "ccc", "ccc" }, new InsertPoint[] { InsertPoint.before("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt, tgt, new String[] { "ccc", "ccc", "ccc" }, new InsertPoint[] { InsertPoint.before("^cc*") });
        lines = Arrays.asList(new String[] { "aaa", "bbb", "ccc", "ccc", "ccc", "ccc", "ccc", "ccc", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, new String[] { "aaa", "AAA" }, new InsertPoint[] { InsertPoint.before("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "AAA", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt, tgt, new String[] { "aaa", "AAA" }, new InsertPoint[] { InsertPoint.before("^aaa") });
        lines = Arrays.asList(new String[] { "aaa", "AAA", "aaa", "AAA", "aaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
    }

    @Test
    public void test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv() throws IOException, URISyntaxException
    {
        File src = getFile("src_weblogicportal-setDomainEnv.cmd.txt");
        File tgt = createTempFile("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv_1_");
        LOGGER.info("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv():: source file: src = " + src);
        LOGGER.info("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv():: target file: tgt = " + tgt);
        String[] toInsert;

        toInsert = new String[] { "rem --------------- Begin APM Tests Configuration ------------------",
                                  "set JAVA_HOME=%SUN_JAVA_HOME%",
                                  "set JAVA_VENDOR=Sun",
                                  "rem --------------- End APM Tests Configuration ------------------",
                                  "" };
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(src, tgt, toInsert, new InsertPoint[] { InsertPoint.before("^@REM We need to reset the value of JAVA_HOME to get it shortened AND ") });

        File tgt2 = createTempFile("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv_2_");
        LOGGER.info("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv():: target file: tgt2 = " + tgt2);
        toInsert = new String[] { "",
                                  "rem --------------- Begin APM Tests Configuration ------------------",
                                  "set MEM_ARGS=-Xms256m -Xmx1024m",
                                  "set MEM_MAX_PERM_SIZE=-XX:PermSize=128m -XX:MaxPermSize=256m",
                                  "rem --------------- End APM Tests Configuration ------------------", };
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt, tgt2, toInsert, new InsertPoint[] { InsertPoint.after("^set MEM_MAX_PERM_SIZE=.*") });

        File tgt3 = createTempFile("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv_3_");
        LOGGER.info("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv():: target file: tgt3 = " + tgt3);
        toInsert = new String[] { "",
                                  "rem --------------- Begin APM Agent Configuration ------------------",
                                  "set JAVA_VM=%JAVA_VM% -javaagent:c:\\beaportal\\wlportal_10.3\\samples\\domains\\portal\\wily\\Agent.jar "
                                  + "-Dcom.wily.introscope.agentProfile=c:\\beaportal\\wlportal_10.3\\samples\\domains\\portal\\wily\\core\\config\\IntroscopeAgent.profile",
                                  "rem --------------- End APM Agent Configuration ------------------" };
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt2, tgt3, toInsert, new InsertPoint[] { InsertPoint.after("^set JAVA_VM=%JAVA_VM% %JAVA_DEBUG% %JAVA_PROFILE%") });

        File tgt4 = createTempFile("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv_4_");
        LOGGER.info("test_insertIntoFileSkipIfAlreadyIncludes_weblogicportalSetDomainEnv():: target file: tgt4 = " + tgt4);
        toInsert = new String[] { "",
                                  "rem --------------- Begin Jmx Configuration ------------------",
                                  "set JAVA_VM=%JAVA_VM% -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false "
                                  + "-Dcom.sun.management.jmxremote.authenticate=false -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote.port=1099",
                                  "rem --------------- End Jmx Configuration ------------------", };
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt3, tgt4, toInsert, new InsertPoint[] { InsertPoint.after("^set JAVA_VM=%JAVA_VM% %JAVA_DEBUG% %JAVA_PROFILE%") });

        assertEquals(Files.readAllLines(getFile("result_weblogicportal-setDomainEnv.cmd.txt").toPath(), Charset.defaultCharset()), Files.readAllLines(tgt4.toPath(), Charset.defaultCharset()));
    }

    @Test
    public void test_updateLinesInFile() throws IOException, URISyntaxException {
        File src = getFile("src.txt");
        File tgt = createTempFile("test_updateLinesInFile_1_");
        LOGGER.info("test_updateLinesInFile():: source file: src = " + src);
        LOGGER.info("test_updateLinesInFile():: target file: tgt = " + tgt);
        List<String> lines;

        ////
        FileUtils.updateLinesInFile(src, tgt);
        assertEquals(Files.readAllLines(src.toPath(), Charset.defaultCharset()), Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));


        try {
            FileUtils.updateLinesInFile(src, tgt, (UpdateLinesOperation) null);
            assertTrue("Exception was expected from FileUtils.updateLinesInFile()", false);
        } catch (Exception e) {}

        FileUtils.updateLinesInFile(src, tgt, (UpdateLinesOperation[]) null);
        assertEquals(Files.readAllLines(src.toPath(), Charset.defaultCharset()), Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt, new UpdateLinesOperation[0]);
        assertEquals(Files.readAllLines(src.toPath(), Charset.defaultCharset()), Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));


        try {
            FileUtils.updateLinesInFile(src, tgt, null, null);
            assertTrue("Exception was expected from FileUtils.updateLinesInFile()", false);
        } catch (Exception e) {}

        try {
            FileUtils.updateLinesInFile(src, tgt, null, new UpdateLinesOperation(null, null));
            assertTrue("Exception was expected from FileUtils.updateLinesInFile()", false);
        } catch (Exception e) {}
        ////



        //// ADD_TO_BEGINNING
        FileUtils.updateLinesInFile(src, tgt, new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "XXX"))));
        lines = Arrays.asList(new String[] { "XXXaaa", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "YYY"))
                                                          )
        );
        lines = Arrays.asList(new String[] { "XXXaaa", "YYYbbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "YYY"))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "ZZZ"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "WWW")))
        );
        lines = Arrays.asList(new String[] { "XXXaaa", "YYYbbb", "ccc", "", "ddd", "eee", "fff", "XXXggg", "YYY", "", "hhh", "iii", "jjj", "", "ZZZaaa", "WWWbbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "YYY"))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "CCC"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, " 123 C "),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, " 123 D "),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, " 123 E "),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, " 123 F "))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "aaa"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "xxx"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "KKK"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "LLL"))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "AAA"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "YYY"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "UUU"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "888"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "999"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "LLL"))),
            new UpdateLinesOperation("", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "555"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "HHH"))),
            new UpdateLinesOperation("ppp", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "PPP"))), // causes no change
            new UpdateLinesOperation("iii", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "ZZZ"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "WWW"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "???")))
        );
        lines = Arrays.asList(new String[] { "XXXaaa", "YYYbbb", "CCCccc", " 123 C ", " 123 D ddd", " 123 E eee", " 123 F fff", "XXXggg", "YYY", "555",
                                             "HHHhhh", "ZZZiii", "WWWjjj", "???", "AAAaaa", "YYYbbb", "UUUaaaccc", "888xxx", "999KKKkkk", "LLLLLLlll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        ////


        //// ADD_TO_END
        FileUtils.updateLinesInFile(src, tgt, new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "XXX"))));
        lines = Arrays.asList(new String[] { "aaaXXX", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "YYY"))
                                                          )
        );
        lines = Arrays.asList(new String[] { "aaaXXX", "bbbYYY", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "YYY"))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "ZZZ"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "WWW")))
        );
        lines = Arrays.asList(new String[] { "aaaXXX", "bbbYYY", "ccc", "", "ddd", "eee", "fff", "gggXXX", "YYY", "", "hhh", "iii", "jjj", "", "aaaZZZ", "bbbWWW", "ccc", "", "kkk", "lll" }); 
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "YYY"))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "CCC"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, " 123 C "),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, " 123 D "),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, " 123 E "),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, " 123 F "))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "aaa"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "xxx"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "KKK"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "LLL"))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "AAA"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "YYY"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "UUU"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "888"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "999"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "LLL"))),
            new UpdateLinesOperation("", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "555"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "HHH"))),
            new UpdateLinesOperation("ppp", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "PPP"))), // causes no change
            new UpdateLinesOperation("iii", Arrays.asList(new OneLineUpdate(UpdateMethod.ADD_TO_END, "ZZZ"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "WWW"),
                                                          new OneLineUpdate(UpdateMethod.ADD_TO_END, "???")))
        );
        lines = Arrays.asList(new String[] { "aaaXXX", "bbbYYY", "cccCCC", " 123 C ", "ddd 123 D ", "eee 123 E ", "fff 123 F ", "gggXXX", "YYY", "555",
                                             "hhhHHH", "iiiZZZ", "jjjWWW", "???", "aaaAAA", "bbbYYY", "cccaaaUUU", "xxx888", "kkkKKK999", "lllLLLLLL" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        ////


        //// REWRITE
        FileUtils.updateLinesInFile(src, tgt, new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "XXX"))));
        lines = Arrays.asList(new String[] { "XXX", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "YYY"))
                                                          )
        );
        lines = Arrays.asList(new String[] { "XXX", "YYY", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "YYY"))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "ZZZ"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "WWW")))
        );
        lines = Arrays.asList(new String[] { "XXX", "YYY", "ccc", "", "ddd", "eee", "fff", "XXX", "YYY", "", "hhh", "iii", "jjj", "", "ZZZ", "WWW", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "XXX"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "YYY"))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "CCC"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, " 123 C "),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, " 123 D "),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, " 123 E "),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, " 123 F "))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "aaa"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "xxx"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "KKK"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "LLL"))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "AAA"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "YYY"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "UUU"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "888"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "999"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "LLL"))),
            new UpdateLinesOperation("", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "555"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "HHH"))),
            new UpdateLinesOperation("ppp", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "PPP"))), // causes no change
            new UpdateLinesOperation("iii", Arrays.asList(new OneLineUpdate(UpdateMethod.REWRITE, "ZZZ"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "WWW"),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "???")))
        );
        lines = Arrays.asList(new String[] { "XXX", "YYY", "CCC", " 123 C ", " 123 D ", " 123 E ", " 123 F ", "XXX", "YYY", "555", "HHH", "ZZZ", "WWW", "???", "AAA", "YYY", "UUU", "888", "999", "LLL" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        ////


        //// CLEAR
        FileUtils.updateLinesInFile(src, tgt, new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR))));
        lines = Arrays.asList(new String[] { "", "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR, "YYY"))
                                                          )
        );
        lines = Arrays.asList(new String[] { "", "", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR, "YYY"))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR, "ZZZ"),
                                                          new OneLineUpdate(UpdateMethod.CLEAR, null)))
        );
        lines = Arrays.asList(new String[] { "", "", "ccc", "", "ddd", "eee", "fff", "", "", "", "hhh", "iii", "jjj", "", "", "", "ccc", "", "kkk", "lll" }); 
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR))),
            new UpdateLinesOperation("", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR))),
            new UpdateLinesOperation("ppp", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR))), // causes no change
            new UpdateLinesOperation("hhh", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR))),
            new UpdateLinesOperation("iii", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.CLEAR)))
        );
        lines = Arrays.asList(new String[] { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        ////


        //// DELETE
        FileUtils.updateLinesInFile(src, tgt, new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE))));
        lines = Arrays.asList(new String[] { "bbb", "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE, "YYY"))
                                                          )
        );
        lines = Arrays.asList(new String[] { "ccc", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE, "YYY"))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE, "ZZZ"),
                                                          new OneLineUpdate(UpdateMethod.DELETE, null)))
        );
        lines = Arrays.asList(new String[] { "ccc", "", "ddd", "eee", "fff", "", "hhh", "iii", "jjj", "", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE, "YYY"))),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE))),
            new UpdateLinesOperation("ccc", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE))),
            new UpdateLinesOperation("aaa", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE))),
            new UpdateLinesOperation("", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE))),
            new UpdateLinesOperation("ppp", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE))), // causes no change
            new UpdateLinesOperation("iii", Arrays.asList(new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE),
                                                          new OneLineUpdate(UpdateMethod.DELETE)))
        );
        lines = Arrays.asList(new String[] {});
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        ////


        //// windowsScriptCommentLine + linuxScriptCommentLine + deleteLines
        FileUtils.updateLinesInFile(src, tgt,
            UpdateLinesOperation.windowsScriptCommentLine("aaa"),
            UpdateLinesOperation.linuxScriptCommentLine("fff"),
            UpdateLinesOperation.windowsScriptCommentLine(""),
            UpdateLinesOperation.linuxScriptCommentLine("")
        );
        lines = Arrays.asList(new String[] { "rem aaa", "bbb", "ccc", "rem ", "ddd", "eee", "# fff", "ggg", "# ", "", "hhh", "iii", "jjj", "", "aaa", "bbb", "ccc", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            UpdateLinesOperation.linuxScriptCommentLine("aaa", 3),
            UpdateLinesOperation.windowsScriptCommentLine("aaa", 2),
            UpdateLinesOperation.windowsScriptCommentLine("# bbb"),
            UpdateLinesOperation.linuxScriptCommentLine("lll", 200),
            UpdateLinesOperation.linuxScriptCommentLine("", 2),
            UpdateLinesOperation.linuxScriptCommentLine("jjj")
        );
        lines = Arrays.asList(new String[] { "# aaa", "rem # bbb", "# ccc", "# ", "# ddd", "eee", "fff", "ggg", "", "", "hhh", "iii", "# jjj", "", "rem aaa", "rem bbb", "ccc", "", "kkk", "# lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            UpdateLinesOperation.deleteLines("ccc", 1),
            UpdateLinesOperation.deleteLines("iii", 3),
            UpdateLinesOperation.deleteLines("ddd", 0), // causes no change
            UpdateLinesOperation.deleteLines("kkk", 300)
        );
        lines = Arrays.asList(new String[] { "aaa", "bbb", "", "ddd", "eee", "fff", "ggg", "", "", "hhh", "aaa", "bbb", "ccc", "" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));

        FileUtils.updateLinesInFile(src, tgt,
            UpdateLinesOperation.linuxScriptCommentLine("aaa", 3),
            UpdateLinesOperation.windowsScriptCommentLine("aaa", 2),
            UpdateLinesOperation.windowsScriptCommentLine("# bbb"),
            UpdateLinesOperation.deleteLines("ddd", 0), // causes no change
            UpdateLinesOperation.deleteLines("ddd", -10), // causes no change
            UpdateLinesOperation.deleteLines("ggg", 4),
            UpdateLinesOperation.linuxScriptCommentLine("lll", 200),
            UpdateLinesOperation.windowsScriptCommentLine("", 2),
            UpdateLinesOperation.linuxScriptCommentLine("jjj"),
            UpdateLinesOperation.deleteLines("kkk", 400)
        );
        lines = Arrays.asList(new String[] { "# aaa", "rem # bbb", "# ccc", "rem ", "rem ddd", "eee", "fff", "iii", "# jjj", "", "rem aaa", "rem bbb", "ccc", "" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        ////


        ////
        FileUtils.updateLinesInFile(src, tgt,
            new UpdateLinesOperation("ddd", UpdateMethod.ADD_TO_BEGINNING, "DDD"),
            new UpdateLinesOperation("ddd", UpdateMethod.ADD_TO_END, "DDD"), // causes no change
            new UpdateLinesOperation("ccc", UpdateMethod.ADD_TO_END, "DDD"),
            new UpdateLinesOperation("ccc", UpdateMethod.ADD_TO_END, "DDD"),
            new UpdateLinesOperation("cccDDD", new OneLineUpdate(UpdateMethod.DELETE),
                                               new OneLineUpdate(UpdateMethod.ADD_TO_END, "xxx")),
            UpdateLinesOperation.windowsScriptCommentLine("xxx", 2),
            UpdateLinesOperation.linuxScriptCommentLine("jjj"),
            UpdateLinesOperation.deleteLines("eee", 2),
            UpdateLinesOperation.linuxScriptCommentLine("bbb", 3),
            new UpdateLinesOperation("ggg", Arrays.asList(new OneLineUpdate(UpdateMethod.CLEAR),
                                                          new OneLineUpdate(UpdateMethod.REWRITE, "RRR"))),
            new UpdateLinesOperation("hhh", new OneLineUpdate(UpdateMethod.REWRITE, "HHH")),
            new UpdateLinesOperation("hhh", new OneLineUpdate(UpdateMethod.DELETE)), // causes no change
            new UpdateLinesOperation("bbb", new OneLineUpdate(UpdateMethod.DELETE)),
            new UpdateLinesOperation("cccDDD", new OneLineUpdate(UpdateMethod.REWRITE, "kkk"))
        );
        lines = Arrays.asList(new String[] { "aaa", "# bbb", "# rem xxx", "# rem DDDddd", "", "RRR", "", "HHH", "iii", "# jjj", "", "aaa", "kkk", "", "kkk", "lll" });
        assertEquals(lines, Files.readAllLines(tgt.toPath(), Charset.defaultCharset()));
        ////
    }

    @Test
    public void test_updateLinesInFile_jboss711StandaloneConf() throws IOException, URISyntaxException {
        File src = getFile("src_jboss711-standalone.conf.bat.txt");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: source file: src = " + src);
        String[] toInsert;

        File tgt = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_01_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt = " + tgt);
        FileUtils.updateLinesInFile(src, tgt, UpdateLinesOperation.windowsScriptCommentLine("if not \\\"x%JAVA_OPTS%\\\" == \\\"x\\\" \\(", 4));

        File tgt2 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_02_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt2 = " + tgt2);
        toInsert = new String[] { "rem set \"JAVA_HOME=E:\\Progra~1\\Java\\jre7\"", "", "set \"JAVA_HOME=S:\\sw\\Java\\jdk1.7.0_45\"" };
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt, tgt2, toInsert, new InsertPoint[] { InsertPoint.after("rem set \"JAVA_HOME=C:\\\\opt\\\\jdk1.6.0_23\\\"") });

        File tgt3 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_03_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt3 = " + tgt3);
        toInsert = new String[] { "", "set \"JAVA=%JAVA_HOME%\\bin\\java\"" };
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt2, tgt3, toInsert, new InsertPoint[] { InsertPoint.after("rem set \"JAVA=C:\\\\opt\\\\jdk1.6.0_23\\\\bin\\\\java\"") });

        File tgt4 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_04_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt4 = " + tgt4);
        FileUtils.updateLinesInFile(tgt3, tgt4, UpdateLinesOperation.windowsScriptCommentLine("set \"JAVA_OPTS=-Xms64M -Xmx512M -XX:MaxPermSize=256M\""));

        File tgt5 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_05_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt5 = " + tgt5);
        FileUtils.updateLinesInFile(tgt4, tgt5, UpdateLinesOperation.windowsScriptCommentLine("set \"JAVA_OPTS=%JAVA_OPTS% -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -Djava.net.preferIPv4Stack=true\""));

        File tgt6 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_06_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt6 = " + tgt6);
        FileUtils.updateLinesInFile(tgt5, tgt6, new UpdateLinesOperation("set \"JAVA_OPTS=%JAVA_OPTS% -Djboss.modules.system.pkgs=org.jboss.byteman\"", UpdateLinesOperation.UpdateMethod.REWRITE,
            "set \"JAVA_OPTS=%JAVA_OPTS% -Djboss.modules.system.pkgs=org.jboss.byteman,com.wily,com.wily.*\""));

        File tgt7 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_07_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt7 = " + tgt7);
        toInsert = new String[] { "set WILY=S:\\sw\\jboss-as-7.1.1.Final\\wily", "set WILY_OPTS=-javaagent:%WILY%\\Agent.jar -Dcom.wily.introscope.agentProfile=%WILY%\\core\\config\\IntroscopeAgent.profile" };
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt6, tgt7, toInsert, new InsertPoint[] { InsertPoint.after("rem set \"JAVA_OPTS=%JAVA_OPTS% -Djboss.modules.lockless=true\"") });

        File tgt8 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_08_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt8 = " + tgt8);
        toInsert = new String[] { "set JAVA_OPTS= -Xms1024m -Xmx1024m -XX:MaxPermSize=256m %WILY_OPTS% %JAVA_OPTS%", "echo %JAVA_OPTS%", "" };
        FileUtils.insertIntoFileSkipIfAlreadyIncludes(tgt7, tgt8, toInsert, new InsertPoint[] { InsertPoint.before(":JAVA_OPTS_SET") });

        File tgt9 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_09_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt9 = " + tgt9);
        FileUtils.updateLinesInFile(tgt8, tgt9, UpdateLinesOperation.windowsScriptCommentLine(":JAVA_OPTS_SET"));

        File tgt10 = createTempFile("test_updateLinesInFile_jboss711StandaloneConf_10_");
        LOGGER.info("test_updateLinesInFile_jboss711StandaloneConf():: target file: tgt10 = " + tgt10);
        FileUtils.updateLinesInFile(tgt9, tgt10, UpdateLinesOperation.deleteLines("set \"JAVA_HOME=c:/SW/Java/jdk1.7.0.51_64\"", 2));

        assertEquals(Files.readAllLines(getFile("result_jboss711-standalone.conf.bat.txt").toPath(), Charset.defaultCharset()), Files.readAllLines(tgt10.toPath(), Charset.defaultCharset()));
    }

    private File getFile(String name) throws URISyntaxException
    {
        return new File(getClass().getResource("/" + name).toURI());
    }

    @SuppressWarnings("unused")
    private File createTempFile() throws IOException, URISyntaxException
    {
        return createTempFile(null);
    }

    private File createTempFile(String prefix) throws IOException, URISyntaxException
    {
        if (prefix == null)
        {
            prefix = "";
        }
        prefix = prefix.trim();
        File tempFile = File.createTempFile(prefix + "tgt_", ".txt", new File(getClass().getResource("/").toURI()));
        //tempFile.deleteOnExit();
        return tempFile;
    }

}
